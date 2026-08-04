package com.trozovka.pocketvdr.core.location

/** One raw position sample from the device, before it's written to the database. */
data class VoyageFix(
    val timestampMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val speedMetersPerSecond: Float?,
    val headingDegrees: Float?,
    val altitudeMeters: Double?,
    val satellitesUsed: Int? = null,
)
