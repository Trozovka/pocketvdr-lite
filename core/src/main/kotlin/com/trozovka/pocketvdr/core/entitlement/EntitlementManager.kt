package com.trozovka.pocketvdr.core.entitlement

import androidx.compose.runtime.Composable

/**
 * Tier differentiation lives entirely behind this interface so :core never links against
 * Gumroad -- Pro's implementation is installed at runtime by its own Application subclass.
 *
 * Marked suspend since Pro's implementation may need a one-time network call the very first
 * time a key is verified (cached forever after); Free's implementation never actually suspends,
 * it's just pure local arithmetic against a first-launch timestamp.
 */
interface EntitlementManager {
    val tierName: String

    /**
     * Null means unlimited historical access (Pro licensed, or Free still inside its trial
     * window). A non-null value means voyages that started before this cutoff are locked for
     * review/export -- logging itself is never gated by this, only reviewing/exporting old data.
     */
    suspend fun historicalAccessCutoffMillis(nowMillis: Long): Long?

    /** One-line status shown on the main screen -- trial countdown, or the lapsed-tier message. */
    suspend fun statusMessage(nowMillis: Long): String

    /** Extension point for tier-specific settings UI (e.g. Pro's license key entry) without forking the shared Settings screen. */
    @Composable
    fun SettingsExtras() {}
}
