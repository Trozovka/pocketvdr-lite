package com.trozovka.pocketvdr.core.entitlement

import com.trozovka.pocketvdr.core.data.VoyageEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoyageAccessTest {

    @Test
    fun `no cutoff means never locked`() {
        val voyage = VoyageEntity(id = 1, startTimeMillis = 0L)
        assertFalse(isVoyageLocked(voyage, activeVoyageId = null, cutoffMillis = null))
    }

    @Test
    fun `voyage older than cutoff is locked`() {
        val voyage = VoyageEntity(id = 1, startTimeMillis = 1000L)
        assertTrue(isVoyageLocked(voyage, activeVoyageId = null, cutoffMillis = 2000L))
    }

    @Test
    fun `voyage newer than cutoff is not locked`() {
        val voyage = VoyageEntity(id = 1, startTimeMillis = 3000L)
        assertFalse(isVoyageLocked(voyage, activeVoyageId = null, cutoffMillis = 2000L))
    }

    @Test
    fun `the currently active voyage is never locked even if it started before the cutoff`() {
        val voyage = VoyageEntity(id = 5, startTimeMillis = 1000L)
        assertFalse(isVoyageLocked(voyage, activeVoyageId = 5L, cutoffMillis = 2000L))
    }
}
