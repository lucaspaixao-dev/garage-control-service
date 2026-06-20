package io.github.lucaspaixaodev.garageservice.infra.output.repository.garage

import io.github.lucaspaixaodev.garageservice.domain.garage.Garage
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalTime
import java.util.UUID
import kotlin.test.assertEquals

class JpaGarageRepositoryTest {

    private val entityRepository = mockk<GarageEntityRepository>()
    private val repository = JpaGarageRepository(garageEntityRepository = entityRepository)

    private fun garage(basePrice: String) =
        Garage.create(
            sector = "A",
            basePrice = BigDecimal(basePrice),
            open = "00:00",
            close = "23:59",
            durationLimit = 1440,
        )

    @Test
    fun `saveAll inserts a new garage keeping its generated id`() {
        val garage = garage(basePrice = "40.5")
        every { entityRepository.findAllBySectorIn(sectors = any()) } returns emptyList()
        val savedEntity = slot<GarageEntity>()
        every { entityRepository.save(capture(savedEntity)) } answers { savedEntity.captured }

        val result = repository.saveAll(garages = listOf(garage))

        assertEquals(1, result.size)
        assertEquals(garage.id, result[GarageSector.A]!!.id)
        assertEquals(garage.id.value, savedEntity.captured.id)
        assertEquals(GarageSector.A, savedEntity.captured.sector)
    }

    @Test
    fun `saveAll reuses the existing id for a known sector and updates its fields`() {
        val existingId = UUID.fromString("cb745afa-aa13-47c5-ac27-347dc169e156")
        val incoming = garage(basePrice = "9.9")
        val existingEntity =
            GarageEntity(
                id = existingId,
                sector = GarageSector.A,
                basePrice = BigDecimal("1.00"),
                openHour = LocalTime.of(0, 0),
                closeHour = LocalTime.of(23, 59),
                durationLimitMinutes = 1440,
            )
        every { entityRepository.findAllBySectorIn(sectors = any()) } returns listOf(existingEntity)
        val savedEntity = slot<GarageEntity>()
        every { entityRepository.save(capture(savedEntity)) } answers { savedEntity.captured }

        val result = repository.saveAll(garages = listOf(incoming))

        // identity stays stable across runs (the existing id wins)...
        assertEquals(existingId, result[GarageSector.A]!!.id.value)
        assertEquals(existingId, savedEntity.captured.id)
        // ...but the persisted fields come from the incoming garage.
        assertEquals("9.90", result[GarageSector.A]!!.basePrice.toString())
        assertEquals(BigDecimal("9.90"), savedEntity.captured.basePrice)
    }
}
