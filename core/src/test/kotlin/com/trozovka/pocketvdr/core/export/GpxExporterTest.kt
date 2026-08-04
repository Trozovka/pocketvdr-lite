package com.trozovka.pocketvdr.core.export

import com.trozovka.pocketvdr.core.data.FixEntity
import com.trozovka.pocketvdr.core.data.FlagEventEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GpxExporterTest {

    private val fixes = listOf(
        FixEntity(voyageId = 1, timestampMillis = 1_700_000_000_000L, latitude = 14.5995, longitude = 120.9842, altitudeMeters = 5.0),
        FixEntity(voyageId = 1, timestampMillis = 1_700_000_010_000L, latitude = 14.6000, longitude = 120.9850, altitudeMeters = null),
    )

    @Test
    fun `output is well-formed GPX with a trkpt per fix`() {
        val gpx = GpxExporter.build("Test voyage", fixes, emptyList())
        assertTrue(gpx.contains("<gpx"))
        assertTrue(gpx.contains("</gpx>"))
        assertTrue(gpx.contains("<trk>"))
        val trkptCount = Regex("<trkpt").findAll(gpx).count()
        assertTrue("expected 2 trkpt elements, found $trkptCount", trkptCount == 2)
    }

    @Test
    fun `missing altitude is omitted, never fabricated as zero`() {
        val gpx = GpxExporter.build("Test voyage", fixes, emptyList())
        assertFalse(gpx.contains("<ele>0.0</ele>"))
    }

    @Test
    fun `flagged events become named waypoints with their note as description`() {
        val flags = listOf(
            FlagEventEntity(voyageId = 1, timestampMillis = 1_700_000_005_000L, latitude = 14.5998, longitude = 120.9846, note = "Fishing boat crossed close"),
        )
        val gpx = GpxExporter.build("Test voyage", fixes, flags)
        assertTrue(gpx.contains("<wpt"))
        assertTrue(gpx.contains("Fishing boat crossed close"))
    }

    @Test
    fun `special characters in voyage name and notes are XML-escaped`() {
        val flags = listOf(
            FlagEventEntity(voyageId = 1, timestampMillis = 1_700_000_005_000L, latitude = 14.0, longitude = 120.0, note = "Crossed <fishing> boat & net"),
        )
        val gpx = GpxExporter.build("Voyage \"A\" & B", fixes, flags)
        assertFalse(gpx.contains("Crossed <fishing>"))
        assertTrue(gpx.contains("Crossed &lt;fishing&gt; boat &amp; net"))
    }
}
