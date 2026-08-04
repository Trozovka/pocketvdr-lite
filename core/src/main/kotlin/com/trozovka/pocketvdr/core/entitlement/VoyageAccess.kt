package com.trozovka.pocketvdr.core.entitlement

import com.trozovka.pocketvdr.core.data.VoyageEntity

/**
 * Pure decision function shared by the voyage list and review screens: a voyage is locked for
 * review/export only if a cutoff is in effect (Free tier, trial lapsed), it isn't the currently
 * active voyage (always reviewable regardless of tier, per the project's "never block logging or
 * its own in-progress review" rule), and it started before the cutoff.
 */
fun isVoyageLocked(voyage: VoyageEntity, activeVoyageId: Long?, cutoffMillis: Long?): Boolean {
    if (cutoffMillis == null) return false
    if (voyage.id == activeVoyageId) return false
    return voyage.startTimeMillis < cutoffMillis
}
