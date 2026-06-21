package io.github.lucaspaixaodev.garageservice.infra.output.repository.spot

import io.github.lucaspaixaodev.garageservice.domain.Id
import io.github.lucaspaixaodev.garageservice.domain.garage.Garage
import io.github.lucaspaixaodev.garageservice.domain.spot.Spot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

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

    @Test
    fun `findByCoordinates reconstructs the spot`() {
        val id = UUID.randomUUID()
        every { entityRepository.findByLatitudeAndLongitude(-23.561684, -46.655981) } returns
                SpotEntity(
                    id = id,
                    externalId = 1,
                    garageId = garage.id.value,
                    latitude = -23.561684,
                    longitude = -46.655981,
                    occupied = true,
                )

        val spot = repository.findByCoordinates(latitude = -23.561684, longitude = -46.655981)

        assertEquals(id, spot?.id?.value)
        assertEquals(1, spot?.externalId?.value)
        assertTrue(spot!!.occupied)
    }

    @Test
    fun `findByCoordinates returns null when there is no spot`() {
        every { entityRepository.findByLatitudeAndLongitude(any(), any()) } returns null

        assertNull(repository.findByCoordinates(latitude = 0.0, longitude = 0.0))
    }

    @Test
    fun `findById reconstructs the spot`() {
        val id = UUID.randomUUID()
        every { entityRepository.findById(id) } returns
                Optional.of(
                    SpotEntity(
                        id = id,
                        externalId = 9,
                        garageId = garage.id.value,
                        latitude = -23.5,
                        longitude = -46.6,
                        occupied = false,
                    ),
                )

        val spot = repository.findById(id = Id(id))

        assertEquals(id, spot?.id?.value)
        assertFalse(spot!!.occupied)
    }

    @Test
    fun `findById returns null when missing`() {
        every { entityRepository.findById(any()) } returns Optional.empty()

        assertNull(repository.findById(id = Id(UUID.randomUUID())))
    }

    @Test
    fun `countTotal and countOccupied delegate to the entity repository`() {
        every { entityRepository.count() } returns 30L
        every { entityRepository.countByOccupiedTrue() } returns 12L

        assertEquals(30, repository.countTotal())
        assertEquals(12, repository.countOccupied())
    }

    @Test
    fun `save persists the spot by its id`() {
        val spot = Spot.occupied(garage = garage, externalId = 1, latitude = -23.5, longitude = -46.6)
        val saved = slot<SpotEntity>()
        every { entityRepository.save(capture(saved)) } answers { saved.captured }

        repository.save(spot = spot)

        assertEquals(spot.id.value, saved.captured.id)
        assertTrue(saved.captured.occupied)
    }
}
