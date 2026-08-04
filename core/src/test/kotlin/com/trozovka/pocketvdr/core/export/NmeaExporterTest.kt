package com.trozovka.pocketvdr.core.export

import com.trozovka.pocketvdr.core.data.FixEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NmeaExporterTest {

    private val sampleFix = FixEntity(
        voyageId = 1,
        timestampMillis = 1_700_000_000_000L,
        latitude = 14.5995,
        longitude = 120.9842,
        speedMetersPerSecond = 2.5f,
        headingDegrees = 90f,
        altitudeMeters = 12.3,
    )

    @Test
    fun `output contains one GGA and one RMC sentence per fix`() {
        val output = NmeaExporter.build(listOf(sampleFix))
        val lines = output.split("\r\n").filter { it.isNotBlank() }
        assertEquals(2, lines.size)
        assertTrue(lines[0].startsWith("\$GPGGA"))
        assertTrue(lines[1].startsWith("\$GPRMC"))
    }

    @Test
    fun `checksum matches a hand-computed XOR`() {
        val body = "GPGGA,000000.000,1435.9700,N,12059.0520,E,1,,,12.3,M,,M,,"
        var expected = 0
        for (c in body) expected = expected xor c.code
        assertEquals("%02X".format(expected), NmeaExporter.checksum(body))
    }

    @Test
    fun `northern and eastern hemispheres reported correctly`() {
        val output = NmeaExporter.build(listOf(sampleFix))
        assertTrue(output.contains(",N,"))
        assertTrue(output.contains(",E,"))
    }

    @Test
    fun `southern and western hemispheres reported correctly`() {
        val southWestFix = sampleFix.copy(latitude = -14.5995, longitude = -120.9842)
        val output = NmeaExporter.build(listOf(southWestFix))
        assertTrue(output.contains(",S,"))
        assertTrue(output.contains(",W,"))
    }

    @Test
    fun `missing altitude and speed are left blank, never fabricated`() {
        val incompleteFix = sampleFix.copy(altitudeMeters = null, speedMetersPerSecond = null, headingDegrees = null)
        val output = NmeaExporter.build(listOf(incompleteFix))
        assertFalse(output.contains("null"))
        val ggaLine = output.split("\r\n").first { it.startsWith("\$GPGGA") }
        assertTrue(ggaLine.contains(",M,,M,,")) // empty altitude field, not a fabricated value
    }

    @Test
    fun `every sentence ends with a valid two-digit hex checksum`() {
        val output = NmeaExporter.build(listOf(sampleFix))
        val checksumPattern = Regex("\\*[0-9A-F]{2}\\r\\n")
        output.split("\r\n").filter { it.isNotBlank() }.forEach { line ->
            assertTrue("Line missing valid checksum: $line", checksumPattern.containsMatchIn("$line\r\n"))
        }
    }
}
