package com.trozovka.pocketvdr.core.export

import com.trozovka.pocketvdr.core.data.FixEntity
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

/**
 * Pure NMEA 0183 builder (GGA + RMC per fix) for exported logs -- separate from any live-feed
 * formatter, since this is a batch export of already-recorded rows, not a real-time stream.
 * Fields this app never actually measured (HDOP) are left empty rather than fabricated, same
 * principle as the sibling GPS Server project's formatter -- satellite count IS measured
 * (via GnssStatus), so that field is filled in when available.
 */
object NmeaExporter {

    fun build(fixes: List<FixEntity>): String {
        val builder = StringBuilder()
        fixes.forEach { fix ->
            builder.append(gga(fix)).append(rmc(fix))
        }
        return builder.toString()
    }

    private fun gga(fix: FixEntity): String {
        val instant = Instant.ofEpochMilli(fix.timestampMillis).atZone(ZoneOffset.UTC)
        val time = "%02d%02d%02d.%03d".format(instant.hour, instant.minute, instant.second, instant.nano / 1_000_000)
        val (latField, latHemi) = formatLatitude(fix.latitude)
        val (lonField, lonHemi) = formatLongitude(fix.longitude)
        val altitude = fix.altitudeMeters?.let { "%.1f".format(Locale.US, it) } ?: ""
        val satellites = fix.satellitesUsed?.let { "%02d".format(it) } ?: ""
        val body = "GPGGA,$time,$latField,$latHemi,$lonField,$lonHemi,1,$satellites,,$altitude,M,,M,,"
        return "\$$body*${checksum(body)}\r\n"
    }

    private fun rmc(fix: FixEntity): String {
        val instant = Instant.ofEpochMilli(fix.timestampMillis).atZone(ZoneOffset.UTC)
        val time = "%02d%02d%02d.%03d".format(instant.hour, instant.minute, instant.second, instant.nano / 1_000_000)
        val date = "%02d%02d%02d".format(instant.dayOfMonth, instant.monthValue, instant.year % 100)
        val (latField, latHemi) = formatLatitude(fix.latitude)
        val (lonField, lonHemi) = formatLongitude(fix.longitude)
        val speedKnots = fix.speedMetersPerSecond?.let { "%.2f".format(Locale.US, it * METERS_PER_SECOND_TO_KNOTS) } ?: ""
        val course = fix.headingDegrees?.let { "%.2f".format(Locale.US, it) } ?: ""
        val body = "GPRMC,$time,A,$latField,$latHemi,$lonField,$lonHemi,$speedKnots,$course,$date,,,A"
        return "\$$body*${checksum(body)}\r\n"
    }

    private fun formatLatitude(lat: Double): Pair<String, Char> {
        val hemisphere = if (lat >= 0) 'N' else 'S'
        val absLat = abs(lat)
        val degrees = floor(absLat).toInt()
        val minutes = (absLat - degrees) * 60
        return "%02d%07.4f".format(Locale.US, degrees, minutes) to hemisphere
    }

    private fun formatLongitude(lon: Double): Pair<String, Char> {
        val hemisphere = if (lon >= 0) 'E' else 'W'
        val absLon = abs(lon)
        val degrees = floor(absLon).toInt()
        val minutes = (absLon - degrees) * 60
        return "%03d%07.4f".format(Locale.US, degrees, minutes) to hemisphere
    }

    internal fun checksum(body: String): String {
        var checksum = 0
        for (char in body) checksum = checksum xor char.code
        return "%02X".format(checksum)
    }

    private const val METERS_PER_SECOND_TO_KNOTS = 1.943844
}
