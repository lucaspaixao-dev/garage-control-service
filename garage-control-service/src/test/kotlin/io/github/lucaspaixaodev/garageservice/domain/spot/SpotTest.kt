package io.github.lucaspaixaodev.garageservice.domain.spot

import io.github.lucaspaixaodev.garageservice.domain.exception.SpotException
import io.github.lucaspaixaodev.garageservice.domain.garage.Garage
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class SpotTest {

    private val garage =
        Garage.create(
            sector = "A",
            basePrice = BigDecimal("40.5"),
            open = "00:00",
            close = "23:59",
            durationLimit = 1440,
        )

    @Test
    fun `occupied builds an occupied spot linked to the garage`() {
        val spot =
            Spot.occupied(
                garage = garage,
                externalId = 1,
                latitude = -23.561684,
                longitude = -46.655981,
            )

        assertNotNull(spot.id.value)
        assertEquals(garage.id, spot.garageId)
        assertEquals(1, spot.externalId.value)
        assertEquals(-23.561684, spot.latitude.value)
        assertEquals(-46.655981, spot.longitude.value)
        assertTrue(spot.occupied)
    }

    @Test
    fun `available builds an unoccupied spot linked to the garage`() {
        val spot =
            Spot.available(
                garage = garage,
                externalId = 2,
                latitude = 0.0,
                longitude = 0.0,
            )

        assertEquals(garage.id, spot.garageId)
        assertFalse(spot.occupied)
    }

    @Test
    fun `restore rebuilds a spot from stored values`() {
        val spotId = "55da270c-9c3d-4854-aaaa-5b9491c7d3d1"
        val garageId = "cb745afa-aa13-47c5-ac27-347dc169e156"

        val spot =
            Spot.restore(
                id = spotId,
                externalId = 5,
                garageId = garageId,
                latitude = 1.0,
                longitude = 2.0,
                occupied = true,
            )

        assertEquals(spotId, spot.id.toString())
        assertEquals(garageId, spot.garageId.toString())
        assertEquals(5, spot.externalId.value)
        assertTrue(spot.occupied)
    }

    @Test
    fun `occupied rejects invalid coordinates`() {
        assertFailsWith<SpotException.InvalidLatitude> {
            Spot.occupied(
                garage = garage,
                externalId = 1,
                latitude = 200.0,
                longitude = 0.0,
            )
        }
    }
}
