package com.trozovka.pocketvdr.core

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.trozovka.pocketvdr.core.data.VoyageRepository
import com.trozovka.pocketvdr.core.logging.VoyageLoggerService
import com.trozovka.pocketvdr.core.settings.AppPreferences
import com.trozovka.pocketvdr.core.ui.MainScreen
import com.trozovka.pocketvdr.core.ui.ReviewScreen
import com.trozovka.pocketvdr.core.ui.SettingsScreen
import com.trozovka.pocketvdr.core.ui.VoyageListScreen
import com.trozovka.toolkit.reliability.LocationReliabilityPermissionFlow
import kotlinx.coroutines.launch

private sealed class Screen {
    object Main : Screen()
    object VoyageList : Screen()
    object Settings : Screen()
    data class Review(val voyageId: Long) : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var preferences: AppPreferences
    private lateinit var repository: VoyageRepository

    private val permissionFlow = LocationReliabilityPermissionFlow(
        activity = this,
        onReady = ::startLogging,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = AppPreferences(applicationContext)
        repository = VoyageRepository(applicationContext)
        setContent {
            MaterialTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.Main) }
                BackHandler(enabled = screen != Screen.Main) { screen = Screen.Main }

                when (val current = screen) {
                    is Screen.Main -> MainScreen(
                        onStartRequested = permissionFlow::begin,
                        onStopRequested = ::stopLogging,
                        onFlagTapped = ::flagCurrentMoment,
                        onNoteSaved = { flagId, note ->
                            lifecycleScope.launch { repository.updateFlagNote(flagId, note) }
                        },
                        onViewVoyages = { screen = Screen.VoyageList },
                        onOpenSettings = { screen = Screen.Settings },
                    )
                    is Screen.VoyageList -> VoyageListScreen(
                        onBack = { screen = Screen.Main },
                        onVoyageSelected = { voyageId -> screen = Screen.Review(voyageId) },
                    )
                    is Screen.Review -> ReviewScreen(
                        voyageId = current.voyageId,
                        onBack = { screen = Screen.VoyageList },
                        onVoyageDeleted = { screen = Screen.VoyageList },
                    )
                    is Screen.Settings -> SettingsScreen(onBack = { screen = Screen.Main })
                }
            }
        }
    }

    private fun startLogging() {
        val intent = Intent(this, VoyageLoggerService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopLogging() {
        val intent = Intent(this, VoyageLoggerService::class.java).apply {
            action = VoyageLoggerService.ACTION_STOP
        }
        startService(intent)
    }

    private fun flagCurrentMoment() {
        val intent = Intent(this, VoyageLoggerService::class.java).apply {
            action = VoyageLoggerService.ACTION_FLAG
        }
        startService(intent)
    }
}
