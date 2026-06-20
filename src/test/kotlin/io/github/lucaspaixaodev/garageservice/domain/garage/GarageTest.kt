package io.github.lucaspaixaodev.garageservice.domain.garage

import io.github.lucaspaixaodev.garageservice.domain.exception.GarageException
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class GarageTest {

    @Test
    fun `create builds a garage with a generated id and value objects`() {
        val garage =
            Garage.create(
                sector = "A",
                basePrice = BigDecimal("40.5"),
                open = "00:00",
                close = "23:59",
                durationLimit = 1440,
            )

        assertNotNull(garage.id.value)
        assertEquals(GarageSector.A, garage.sector)
        assertEquals("40.50", garage.basePrice.toString())
        assertEquals("00:00", garage.openHour.toString())
        assertEquals("23:59", garage.closeHour.toString())
        assertEquals(1440, garage.durationLimit.minutes)
    }

    @Test
    fun `restore rebuilds a garage with the given id`() {
        val id = "cb745afa-aa13-47c5-ac27-347dc169e156"

        val garage =
            Garage.restore(
                id = id,
                sector = "B",
                basePrice = BigDecimal("4.1"),
                open = "08:00",
                close = "23:59",
                durationLimit = 60,
            )

        assertEquals(id, garage.id.toString())
        assertEquals(GarageSector.B, garage.sector)
    }

    @Test
    fun `create rejects a close hour before the open hour`() {
        val exception =
            assertFailsWith<GarageException.CloseHourBeforeOpenHour> {
                Garage.create(
                    sector = "A",
                    basePrice = BigDecimal.TEN,
                    open = "10:00",
                    close = "09:00",
                    durationLimit = 60,
                )
            }

        assertEquals("Close hour '09:00' must not be before open hour '10:00'", exception.message)
    }

    @Test
    fun `create allows an equal open and close hour`() {
        val garage =
            Garage.create(
                sector = "A",
                basePrice = BigDecimal.ONE,
                open = "08:00",
                close = "08:00",
                durationLimit = 30,
            )

        assertEquals("08:00", garage.openHour.toString())
        assertEquals("08:00", garage.closeHour.toString())
    }
}
