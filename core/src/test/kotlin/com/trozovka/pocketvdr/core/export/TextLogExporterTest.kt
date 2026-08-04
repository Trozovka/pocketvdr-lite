package com.trozovka.pocketvdr.core.export

import com.trozovka.pocketvdr.core.data.FixEntity
import com.trozovka.pocketvdr.core.data.FlagEventEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextLogExporterTest {

    private val fixes = listOf(
        FixEntity(
            voyageId = 1,
            timestampMillis = 1_700_000_000_000L,
            latitude = 14.5995,
            longitude = 120.9842,
            speedMetersPerSecond = 2.5f,
            headingDegrees = 90f,
            altitudeMeters = 5.0,
            satellitesUsed = 8,
        ),
    )

    @Test
    fun `output uses CRLF line endings throughout, for plain Notepad compatibility`() {
        val text = TextLogExporter.build("Test voyage", fixes, emptyList())
        assertTrue(text.contains("\r\n"))
        // Every bare newline must be preceded by a carriage return -- no lone LF anywhere.
        val bareLineFeed = Regex("(?<!\r)\n")
        assertFalse(bareLineFeed.containsMatchIn(text))
    }

    @Test
    fun `track fix line includes lat-lon hemisphere format, not raw signed decimals`() {
        val text = TextLogExporter.build("Test voyage", fixes, emptyList())
        assertTrue(text.contains("14.59950 N"))
        assertTrue(text.contains("120.98420 E"))
    }

    @Test
    fun `marked events section only appears when there are marked events`() {
        val withoutFlags = TextLogExporter.build("Test voyage", fixes, emptyList())
        assertFalse(withoutFlags.contains("Marked events"))

        val flags = listOf(
            FlagEventEntity(voyageId = 1, timestampMillis = 1_700_000_000_500L, latitude = 14.6, longitude = 120.99, note = "Engine trouble"),
        )
        val withFlags = TextLogExporter.build("Test voyage", fixes, flags)
        assertTrue(withFlags.contains("Marked events"))
        assertTrue(withFlags.contains("Engine trouble"))
    }

    @Test
    fun `missing optional fields are omitted, never fabricated`() {
        val incompleteFix = fixes[0].copy(speedMetersPerSecond = null, headingDegrees = null, altitudeMeters = null, satellitesUsed = null)
        val text = TextLogExporter.build("Test voyage", listOf(incompleteFix), emptyList())
        assertFalse(text.contains("Speed:"))
        assertFalse(text.contains("Heading:"))
        assertFalse(text.contains("Satellites:"))
    }
}
