package io.github.lucaspaixaodev.garageservice.application.garage.usecase

import io.github.lucaspaixaodev.garageservice.application.garage.gateway.GarageData
import io.github.lucaspaixaodev.garageservice.application.garage.gateway.GarageGateway
import io.github.lucaspaixaodev.garageservice.application.garage.gateway.GarageInfo
import io.github.lucaspaixaodev.garageservice.application.garage.gateway.SpotInfo
import io.github.lucaspaixaodev.garageservice.application.garage.repository.GarageRepository
import io.github.lucaspaixaodev.garageservice.application.spot.repository.SpotRepository
import io.github.lucaspaixaodev.garageservice.domain.exception.SpotException
import io.github.lucaspaixaodev.garageservice.domain.garage.Garage
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.spot.Spot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SetupGarageUseCaseTest {

    private val garageGateway = mockk<GarageGateway>()
    private val garageRepository = mockk<GarageRepository>()
    private val spotRepository = mockk<SpotRepository>()
    private val useCase =
        SetupGarageUseCase(
            garageGateway = garageGateway,
            garageRepository = garageRepository,
            spotRepository = spotRepository,
        )

    private fun garage(sector: String) =
        Garage.create(
            sector = sector,
            basePrice = BigDecimal("40.5"),
            open = "00:00",
            close = "23:59",
            durationLimit = 1440,
        )

    @Test
    fun `execute fetches, links spots to garages and returns counts`() {
        val data =
            GarageData(
                garages =
                    listOf(
                        GarageInfo(sector = "A", basePrice = BigDecimal("40.5"), openHour = "00:00", closeHour = "23:59", durationLimitMinutes = 1440),
                        GarageInfo(sector = "B", basePrice = BigDecimal("4.1"), openHour = "08:00", closeHour = "23:59", durationLimitMinutes = 60),
                    ),
                spots =
                    listOf(
                        SpotInfo(externalId = 1, sector = "A", latitude = -23.5, longitude = -46.6, occupied = true),
                        SpotInfo(externalId = 11, sector = "B", latitude = -23.4, longitude = -46.5, occupied = false),
                    ),
            )
        val garageA = garage(sector = "A")
        val garageB = garage(sector = "B")
        every { garageGateway.fetch() } returns data
        every { garageRepository.saveAll(garages = any()) } returns mapOf(GarageSector.A to garageA, GarageSector.B to garageB)
        val savedSpots = slot<List<Spot>>()
        every { spotRepository.saveAll(spots = capture(savedSpots)) } just Runs

        val result = useCase.execute()

        assertEquals(2, result.garages)
        assertEquals(2, result.spots)
        verify(exactly = 1) { garageGateway.fetch() }
        verify(exactly = 1) { garageRepository.saveAll(garages = any()) }
        verify(exactly = 1) { spotRepository.saveAll(spots = any()) }

        val spots = savedSpots.captured
        assertEquals(garageA.id, spots.single { it.externalId.value == 1 }.garageId)
        assertEquals(garageB.id, spots.single { it.externalId.value == 11 }.garageId)
        assertTrue(spots.single { it.externalId.value == 1 }.occupied)
        assertFalse(spots.single { it.externalId.value == 11 }.occupied)
    }

    @Test
    fun `execute fails when a spot references a sector without a garage`() {
        val data =
            GarageData(
                garages = listOf(GarageInfo(sector = "A", basePrice = BigDecimal("40.5"), openHour = "00:00", closeHour = "23:59", durationLimitMinutes = 1440)),
                spots = listOf(SpotInfo(externalId = 11, sector = "B", latitude = -23.4, longitude = -46.5, occupied = true)),
            )
        every { garageGateway.fetch() } returns data
        every { garageRepository.saveAll(garages = any()) } returns mapOf(GarageSector.A to garage(sector = "A"))

        val exception = assertFailsWith<SpotException.GarageNotFoundForSector> { useCase.execute() }

        assertEquals("Spot '11' references sector 'B' that has no garage", exception.message)
        verify(exactly = 0) { spotRepository.saveAll(spots = any()) }
    }
}
