package com.trozovka.pocketvdr.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PositionFormatTest {

    @Test
    fun `positive latitude and longitude report North and East`() {
        assertEquals("14.59950 N, 120.98420 E", formatLatLon(14.5995, 120.9842))
    }

    @Test
    fun `negative latitude reports South`() {
        assertEquals("14.59950 S, 120.98420 E", formatLatLon(-14.5995, 120.9842))
    }

    @Test
    fun `negative longitude reports West`() {
        assertEquals("14.59950 N, 120.98420 W", formatLatLon(14.5995, -120.9842))
    }

    @Test
    fun `both negative reports South and West`() {
        assertEquals("14.59950 S, 120.98420 W", formatLatLon(-14.5995, -120.9842))
    }
}
