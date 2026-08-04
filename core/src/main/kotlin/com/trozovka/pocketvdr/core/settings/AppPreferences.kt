package com.trozovka.pocketvdr.core.settings

import android.content.Context

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var logIntervalSeconds: Int
        get() = prefs.getInt(KEY_LOG_INTERVAL_SECONDS, DEFAULT_LOG_INTERVAL_SECONDS)
        set(value) = prefs.edit().putInt(KEY_LOG_INTERVAL_SECONDS, value).apply()

    var useMetricUnits: Boolean
        get() = prefs.getBoolean(KEY_METRIC_UNITS, true)
        set(value) = prefs.edit().putBoolean(KEY_METRIC_UNITS, value).apply()

    companion object {
        private const val PREFS_NAME = "pocketvdr_app_prefs"
        private const val KEY_LOG_INTERVAL_SECONDS = "log_interval_seconds"
        private const val KEY_METRIC_UNITS = "use_metric_units"
        const val DEFAULT_LOG_INTERVAL_SECONDS = 7
        const val MIN_LOG_INTERVAL_SECONDS = 5
        const val MAX_LOG_INTERVAL_SECONDS = 60
    }
}
