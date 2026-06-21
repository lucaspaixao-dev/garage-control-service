package io.github.lucaspaixaodev.garageservice.domain.garage.valueobject

import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class OccupancyRateTest {

    @Test
    fun `below 25 percent gives a 10 percent discount`() {
        assertEquals(BigDecimal("0.90"), OccupancyRate.of(occupied = 2, total = 10).priceMultiplier())
    }

    @Test
    fun `exactly 25 percent is base price`() {
        assertEquals(BigDecimal("1.00"), OccupancyRate.of(occupied = 25, total = 100).priceMultiplier())
    }

    @Test
    fun `up to 50 percent is base price`() {
        assertEquals(BigDecimal("1.00"), OccupancyRate.of(occupied = 5, total = 10).priceMultiplier())
    }

    @Test
    fun `up to 75 percent adds 10 percent`() {
        assertEquals(BigDecimal("1.10"), OccupancyRate.of(occupied = 75, total = 100).priceMultiplier())
    }

    @Test
    fun `above 75 percent up to full adds 25 percent`() {
        assertEquals(BigDecimal("1.25"), OccupancyRate.of(occupied = 8, total = 10).priceMultiplier())
        assertEquals(BigDecimal("1.25"), OccupancyRate.of(occupied = 10, total = 10).priceMultiplier())
    }

    @Test
    fun `isFull is true only at 100 percent`() {
        assertFalse(OccupancyRate.of(occupied = 9, total = 10).isFull)
        assertTrue(OccupancyRate.of(occupied = 10, total = 10).isFull)
    }

    @Test
    fun `an empty garage with no spots is treated as zero occupancy`() {
        val rate = OccupancyRate.of(occupied = 0, total = 0)

        assertEquals(BigDecimal("0.90"), rate.priceMultiplier())
        assertFalse(rate.isFull)
    }
}
