package com.trozovka.pocketvdr.core.export

import com.trozovka.pocketvdr.core.data.FixEntity
import com.trozovka.pocketvdr.core.data.FlagEventEntity
import java.time.Instant

/**
 * Pure GPX 1.1 builder. No Android dependency, no I/O -- takes already-loaded rows, returns a
 * string. Flagged events become waypoints (with their note as the description) so they show up
 * as named points alongside the track in any GPX viewer, not just inside this app's own review UI.
 */
object GpxExporter {

    fun build(voyageName: String, fixes: List<FixEntity>, flags: List<FlagEventEntity>): String {
        val builder = StringBuilder()
        builder.append("""<?xml version="1.0" encoding="UTF-8"?>""").append('\n')
        builder.append(
            """<gpx version="1.1" creator="PocketVDR" xmlns="http://www.topografix.com/GPX/1/1">""",
        ).append('\n')

        flags.forEach { flag ->
            val lat = flag.latitude
            val lon = flag.longitude
            if (lat != null && lon != null) {
                builder.append("  <wpt lat=\"$lat\" lon=\"$lon\">\n")
                builder.append("    <time>${isoTime(flag.timestampMillis)}</time>\n")
                builder.append("    <name>${escapeXml(flagLabel(flag))}</name>\n")
                if (!flag.note.isNullOrBlank()) {
                    builder.append("    <desc>${escapeXml(flag.note)}</desc>\n")
                }
                builder.append("  </wpt>\n")
            }
        }

        builder.append("  <trk>\n")
        builder.append("    <name>${escapeXml(voyageName)}</name>\n")
        builder.append("    <trkseg>\n")
        fixes.forEach { fix ->
            builder.append("      <trkpt lat=\"${fix.latitude}\" lon=\"${fix.longitude}\">\n")
            fix.altitudeMeters?.let { builder.append("        <ele>$it</ele>\n") }
            builder.append("        <time>${isoTime(fix.timestampMillis)}</time>\n")
            fix.satellitesUsed?.let { builder.append("        <sat>$it</sat>\n") }
            builder.append("      </trkpt>\n")
        }
        builder.append("    </trkseg>\n")
        builder.append("  </trk>\n")
        builder.append("</gpx>\n")
        return builder.toString()
    }

    private fun flagLabel(flag: FlagEventEntity): String =
        flag.note?.takeIf { it.isNotBlank() } ?: "Marked moment"

    private fun isoTime(millis: Long): String = Instant.ofEpochMilli(millis).toString()

    private fun escapeXml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
