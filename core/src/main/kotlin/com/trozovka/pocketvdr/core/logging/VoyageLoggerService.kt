package com.trozovka.pocketvdr.core.logging

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.trozovka.pocketvdr.core.data.FixEntity
import com.trozovka.pocketvdr.core.data.FlagEventEntity
import com.trozovka.pocketvdr.core.data.PocketVdrDatabase
import com.trozovka.pocketvdr.core.data.VoyageEntity
import com.trozovka.pocketvdr.core.location.LocationSource
import com.trozovka.pocketvdr.core.location.VoyageFix
import com.trozovka.pocketvdr.core.settings.AppPreferences
import com.trozovka.toolkit.reliability.WakeLockController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Foreground service owning the wake lock, the location callback, and the batched write path
 * into local SQLite (via Room). Fixes are buffered in memory and flushed periodically (or once
 * the buffer fills), rather than one disk write per fix -- a multi-hour voyage at a 5-10s
 * interval would otherwise mean thousands of tiny writes and real flash wear/jank.
 */
class VoyageLoggerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var flushJob: Job? = null
    private lateinit var wakeLock: WakeLockController
    private lateinit var locationSource: LocationSource
    private lateinit var database: PocketVdrDatabase

    private val bufferMutex = Mutex()
    private val buffer = mutableListOf<FixEntity>()

    @Volatile
    private var activeVoyageId: Long = -1L

    override fun onCreate() {
        super.onCreate()
        database = PocketVdrDatabase.getInstance(applicationContext)
        wakeLock = WakeLockController(applicationContext, "$packageName:VoyageLoggerWakeLock")
        startForegroundWithNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopLogging()
                return START_NOT_STICKY
            }
            ACTION_FLAG -> {
                flagCurrentMoment(intent.getStringExtra(EXTRA_NOTE))
                return START_STICKY
            }
            else -> {
                if (!_isRunning.value) {
                    startLogging()
                }
            }
        }
        return START_STICKY
    }

    private fun flagCurrentMoment(note: String?) {
        val voyageId = activeVoyageId
        if (voyageId == -1L) return
        val fix = _latestFix.value
        scope.launch {
            val flagId = database.flagEventDao().insert(
                FlagEventEntity(
                    voyageId = voyageId,
                    timestampMillis = System.currentTimeMillis(),
                    latitude = fix?.latitude,
                    longitude = fix?.longitude,
                    note = note?.takeIf { it.isNotBlank() },
                ),
            )
            _lastFlagId.value = flagId
        }
    }

    private fun startLogging() {
        wakeLock.acquire()
        val preferences = AppPreferences(applicationContext)
        val intervalMillis = preferences.logIntervalSeconds * 1000L

        _isRunning.value = true
        _fixCount.value = 0

        scope.launch {
            val voyageId = database.voyageDao().insert(VoyageEntity(startTimeMillis = System.currentTimeMillis()))
            activeVoyageId = voyageId
            _activeVoyageId.value = voyageId

            locationSource = LocationSource(applicationContext, intervalMillis)
            locationSource.start()

            launch {
                locationSource.fixes.collect { fix ->
                    fix?.let { onNewFix(voyageId, it) }
                }
            }

            flushJob = launch {
                while (isActive) {
                    delay(FLUSH_INTERVAL_MILLIS)
                    flush()
                }
            }
        }
    }

    private suspend fun onNewFix(voyageId: Long, fix: VoyageFix) {
        val shouldFlushNow = bufferMutex.withLock {
            buffer.add(
                FixEntity(
                    voyageId = voyageId,
                    timestampMillis = fix.timestampMillis,
                    latitude = fix.latitude,
                    longitude = fix.longitude,
                    speedMetersPerSecond = fix.speedMetersPerSecond,
                    headingDegrees = fix.headingDegrees,
                    altitudeMeters = fix.altitudeMeters,
                ),
            )
            buffer.size >= FLUSH_BATCH_SIZE
        }
        _latestFix.value = fix
        if (shouldFlushNow) flush()
    }

    private suspend fun flush() {
        val toWrite = bufferMutex.withLock {
            if (buffer.isEmpty()) return
            val copy = buffer.toList()
            buffer.clear()
            copy
        }
        database.fixDao().insertAll(toWrite)
        _fixCount.value = _fixCount.value + toWrite.size
    }

    private fun stopLogging() {
        scope.launch {
            flushJob?.cancel()
            if (::locationSource.isInitialized) locationSource.stop()
            flush()

            val voyageId = activeVoyageId
            if (voyageId != -1L) {
                database.voyageDao().getById(voyageId)?.let { voyage ->
                    database.voyageDao().update(voyage.copy(endTimeMillis = System.currentTimeMillis()))
                }
            }

            wakeLock.release()
            activeVoyageId = -1L
            _isRunning.value = false
            _activeVoyageId.value = null
            _latestFix.value = null
            stopSelf()
        }
    }

    private fun startForegroundWithNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Voyage Logging", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PocketVDR logging voyage")
            .setContentText("Recording position locally -- no network involved")
            .setSmallIcon(com.trozovka.pocketvdr.core.R.drawable.ic_notification)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "voyage_logger_channel"
        private const val NOTIFICATION_ID = 1
        private const val FLUSH_INTERVAL_MILLIS = 30_000L
        private const val FLUSH_BATCH_SIZE = 20
        const val ACTION_STOP = "com.trozovka.pocketvdr.core.action.STOP"
        const val ACTION_FLAG = "com.trozovka.pocketvdr.core.action.FLAG"
        const val EXTRA_NOTE = "extra_note"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _lastFlagId = MutableStateFlow<Long?>(null)
        val lastFlagId: StateFlow<Long?> = _lastFlagId.asStateFlow()

        private val _activeVoyageId = MutableStateFlow<Long?>(null)
        val activeVoyageId: StateFlow<Long?> = _activeVoyageId.asStateFlow()

        private val _fixCount = MutableStateFlow(0)
        val fixCount: StateFlow<Int> = _fixCount.asStateFlow()

        private val _latestFix = MutableStateFlow<VoyageFix?>(null)
        val latestFix: StateFlow<VoyageFix?> = _latestFix.asStateFlow()
    }
}
