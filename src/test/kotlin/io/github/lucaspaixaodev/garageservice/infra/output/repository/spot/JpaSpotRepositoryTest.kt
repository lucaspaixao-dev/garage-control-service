package io.github.lucaspaixaodev.garageservice.infra.output.repository.spot

import io.github.lucaspaixaodev.garageservice.domain.garage.Garage
import io.github.lucaspaixaodev.garageservice.domain.spot.Spot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JpaSpotRepositoryTest {

    private val entityRepository = mockk<SpotEntityRepository>()
    private val repository = JpaSpotRepository(spotEntityRepository = entityRepository)

    private val garage =
        Garage.create(
            sector = "A",
            basePrice = BigDecimal("40.5"),
            open = "00:00",
            close = "23:59",
            durationLimit = 1440,
        )

    @Test
    fun `saveAll inserts new spots with their generated ids`() {
        val spot = Spot.occupied(garage = garage, externalId = 1, latitude = -23.5, longitude = -46.6)
        every { entityRepository.findAllByExternalIdIn(externalIds = any()) } returns emptyList()
        val savedEntities = slot<List<SpotEntity>>()
        every { entityRepository.saveAll(capture(savedEntities)) } returns emptyList()

        repository.saveAll(spots = listOf(spot))

        val entity = savedEntities.captured.single()
        assertEquals(spot.id.value, entity.id)
        assertEquals(1, entity.externalId)
        assertEquals(garage.id.value, entity.garageId)
        assertTrue(entity.occupied)
    }

    @Test
    fun `saveAll reuses the existing id for a known external id`() {
        val existingId = UUID.fromString("55da270c-9c3d-4854-aaaa-5b9491c7d3d1")
        val spot = Spot.occupied(garage = garage, externalId = 7, latitude = -23.5, longitude = -46.6)
        val existingEntity =
            SpotEntity(
                id = existingId,
                externalId = 7,
                garageId = garage.id.value,
                latitude = 0.0,
                longitude = 0.0,
                occupied = false,
            )
        every { entityRepository.findAllByExternalIdIn(externalIds = any()) } returns listOf(existingEntity)
        val savedEntities = slot<List<SpotEntity>>()
        every { entityRepository.saveAll(capture(savedEntities)) } returns emptyList()

        repository.saveAll(spots = listOf(spot))

        assertEquals(existingId, savedEntities.captured.single().id)
    }
}
