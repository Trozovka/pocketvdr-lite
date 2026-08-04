package com.trozovka.pocketvdr.core.util

import kotlin.math.abs

/**
 * Mariner-style "xx.xxxxx N/S, yyy.yyyyy E/W" instead of raw signed decimal degrees.
 * Negative latitude is South, negative longitude is West -- standard convention, so the sign
 * carries the same meaning it always did; this just spells the hemisphere out instead of
 * relying on the reader to know the sign convention.
 */
fun formatLatLon(latitude: Double, longitude: Double): String {
    val latHemisphere = if (latitude >= 0) "N" else "S"
    val lonHemisphere = if (longitude >= 0) "E" else "W"
    return "%.5f %s, %.5f %s".format(abs(latitude), latHemisphere, abs(longitude), lonHemisphere)
}
