package com.trozovka.pocketvdr.core.export

import com.trozovka.pocketvdr.core.data.FixEntity
import com.trozovka.pocketvdr.core.data.FlagEventEntity
import com.trozovka.pocketvdr.core.util.formatLatLon
import com.trozovka.pocketvdr.core.util.formatUtcTimestamp

/**
 * A plain, human-readable log meant to be opened directly in Notepad, WordPad, or similar --
 * not for import into any navigation or charting software (that's what GPX/NMEA are for). Uses
 * CRLF line endings throughout, since plain Notepad on Windows only renders bare LF as one long
 * unbroken line rather than separate lines.
 */
object TextLogExporter {

    private const val CRLF = "\r\n"

    fun build(voyageName: String, fixes: List<FixEntity>, flags: List<FlagEventEntity>): String {
        val builder = StringBuilder()
        builder.append("PocketVDR Voyage Log -- $voyageName").append(CRLF)
        builder.append("Not a type-approved VDR or S-VDR under SOLAS. Personal record-keeping only.").append(CRLF)
        builder.append(CRLF)

        if (flags.isNotEmpty()) {
            builder.append("Marked events:").append(CRLF)
            flags.forEach { flag ->
                val position = if (flag.latitude != null && flag.longitude != null) {
                    formatLatLon(flag.latitude, flag.longitude)
                } else {
                    "position unknown"
                }
                builder.append("  ${formatUtcTimestamp(flag.timestampMillis)} -- $position")
                if (!flag.note.isNullOrBlank()) {
                    builder.append(" -- ${flag.note}")
                }
                builder.append(CRLF)
            }
            builder.append(CRLF)
        }

        builder.append("Track (${fixes.size} fixes):").append(CRLF)
        fixes.forEach { fix ->
            builder.append("  ${formatUtcTimestamp(fix.timestampMillis)} -- ${formatLatLon(fix.latitude, fix.longitude)}")
            fix.speedMetersPerSecond?.let { builder.append(" -- Speed: %.1f m/s".format(it)) }
            fix.headingDegrees?.let { builder.append(" -- Heading: %.0f deg".format(it)) }
            fix.altitudeMeters?.let { builder.append(" -- Alt: %.0f m".format(it)) }
            fix.satellitesUsed?.let { builder.append(" -- Satellites: $it") }
            builder.append(CRLF)
        }

        return builder.toString()
    }
}
