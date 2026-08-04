package com.trozovka.pocketvdr.core.entitlement

import android.content.Context

/**
 * Free for 30 days from first launch (tracked locally, offline -- a determined user could reset
 * the clock by reinstalling, an accepted tradeoff for a tool that must work with zero
 * connectivity rather than over-engineering anti-piracy for a personal-scale project). After the
 * trial lapses, logging stays unlimited forever; only review/export of anything older than the
 * rolling window (or outside the current voyage) locks.
 */
class FreeEntitlementManager(context: Context) : EntitlementManager {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val firstLaunchMillis: Long = prefs.getLong(KEY_FIRST_LAUNCH, -1L).let { stored ->
        if (stored != -1L) {
            stored
        } else {
            System.currentTimeMillis().also { now -> prefs.edit().putLong(KEY_FIRST_LAUNCH, now).apply() }
        }
    }

    override val tierName: String = "Free"

    private fun isTrialActive(nowMillis: Long): Boolean = nowMillis - firstLaunchMillis < TRIAL_DURATION_MILLIS

    override suspend fun historicalAccessCutoffMillis(nowMillis: Long): Long? =
        if (isTrialActive(nowMillis)) null else nowMillis - ROLLING_WINDOW_MILLIS

    override suspend fun statusMessage(nowMillis: Long): String {
        if (isTrialActive(nowMillis)) {
            val remainingMillis = TRIAL_DURATION_MILLIS - (nowMillis - firstLaunchMillis)
            val remainingDays = (remainingMillis / MILLIS_PER_DAY).toInt() + 1
            return "Free trial: $remainingDays day${if (remainingDays == 1) "" else "s"} left -- full history review/export"
        }
        return "Free tier: review/export limited to the last 24h or the current voyage"
    }

    companion object {
        private const val PREFS_NAME = "pocketvdr_entitlement"
        private const val KEY_FIRST_LAUNCH = "first_launch_millis"
        private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000
        const val TRIAL_DURATION_MILLIS = 30L * MILLIS_PER_DAY
        const val ROLLING_WINDOW_MILLIS = MILLIS_PER_DAY
    }
}
