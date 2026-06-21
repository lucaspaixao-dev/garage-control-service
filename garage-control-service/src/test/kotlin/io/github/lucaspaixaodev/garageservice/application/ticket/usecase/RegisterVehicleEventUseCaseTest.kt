package io.github.lucaspaixaodev.garageservice.application.ticket.usecase

import io.github.lucaspaixaodev.garageservice.application.spot.repository.SpotRepository
import io.github.lucaspaixaodev.garageservice.application.ticket.repository.TicketRepository
import io.github.lucaspaixaodev.garageservice.domain.Id
import io.github.lucaspaixaodev.garageservice.domain.exception.SpotException
import io.github.lucaspaixaodev.garageservice.domain.exception.TicketException
import io.github.lucaspaixaodev.garageservice.domain.spot.Spot
import io.github.lucaspaixaodev.garageservice.domain.ticket.Ticket
import io.github.lucaspaixaodev.garageservice.domain.ticket.TicketEvent
import io.github.lucaspaixaodev.garageservice.domain.ticket.TicketEventTime
import io.github.lucaspaixaodev.garageservice.domain.ticket.TicketEventType
import io.github.lucaspaixaodev.garageservice.domain.ticket.TicketStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegisterVehicleEventUseCaseTest {

    private val ticketRepository = mockk<TicketRepository>()
    private val spotRepository = mockk<SpotRepository>()
    private val useCase =
        RegisterVehicleEventUseCase(ticketRepository = ticketRepository, spotRepository = spotRepository)

    private val entryTime = LocalDateTime.parse("2025-01-01T12:00:00")

    private fun spot(id: Id, occupied: Boolean): Spot =
        Spot.restore(
            id = id.toString(),
            externalId = 1,
            garageId = UUID.randomUUID().toString(),
            latitude = -23.561684,
            longitude = -46.655981,
            occupied = occupied,
        )

    @Test
    fun `entry opens and saves a new ticket`() {
        val saved = slot<Ticket>()
        every { ticketRepository.save(ticket = capture(saved)) } answers { saved.captured }

        useCase.execute(
            command = VehicleEventCommand(type = TicketEventType.ENTRY, licensePlate = "ZUL0001", entryTime = entryTime),
        )

        assertEquals("ZUL0001", saved.captured.vehicle.licensePlate)
        assertEquals(TicketStatus.OPEN, saved.captured.status)
        assertNull(saved.captured.spotId)
        verify(exactly = 1) { ticketRepository.save(ticket = any()) }
    }

    @Test
    fun `parked resolves the spot, occupies it and attaches it to the open ticket`() {
        val spotId = Id.generate()
        val foundSpot = spot(id = spotId, occupied = false)
        every { ticketRepository.findOpenByLicensePlate(licensePlate = "ZUL0001") } returns
            Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime)
        every { spotRepository.findByCoordinates(latitude = -23.561684, longitude = -46.655981) } returns foundSpot
        val savedSpot = slot<Spot>()
        every { spotRepository.save(spot = capture(savedSpot)) } answers { savedSpot.captured }
        val savedTicket = slot<Ticket>()
        every { ticketRepository.save(ticket = capture(savedTicket)) } answers { savedTicket.captured }

        useCase.execute(
            command =
                VehicleEventCommand(
                    type = TicketEventType.PARKED,
                    licensePlate = "ZUL0001",
                    latitude = -23.561684,
                    longitude = -46.655981,
                ),
        )

        assertTrue(savedSpot.captured.occupied) // spot was occupied
        assertEquals(spotId, savedTicket.captured.spotId)
        assertEquals(TicketEventType.PARKED, savedTicket.captured.events.last().type)
    }

    @Test
    fun `parked without an open ticket throws`() {
        every { ticketRepository.findOpenByLicensePlate(licensePlate = "ZUL0001") } returns null

        assertThrows<TicketException.OpenTicketNotFound> {
            useCase.execute(
                command =
                    VehicleEventCommand(
                        type = TicketEventType.PARKED,
                        licensePlate = "ZUL0001",
                        latitude = -23.5,
                        longitude = -46.6,
                    ),
            )
        }
    }

    @Test
    fun `parked with an unknown spot throws`() {
        every { ticketRepository.findOpenByLicensePlate(licensePlate = "ZUL0001") } returns
            Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime)
        every { spotRepository.findByCoordinates(latitude = -23.5, longitude = -46.6) } returns null

        assertThrows<SpotException.NotFoundForCoordinates> {
            useCase.execute(
                command =
                    VehicleEventCommand(
                        type = TicketEventType.PARKED,
                        licensePlate = "ZUL0001",
                        latitude = -23.5,
                        longitude = -46.6,
                    ),
            )
        }
    }

    @Test
    fun `exit releases the spot and closes a parked ticket`() {
        val spotId = Id.generate()
        val parkedTicket =
            Ticket.restore(
                id = UUID.randomUUID().toString(),
                licensePlate = "ZUL0001",
                spotId = spotId.toString(),
                status = TicketStatus.OPEN,
                events = listOf(TicketEvent(type = TicketEventType.ENTRY, time = TicketEventTime(entryTime))),
            )
        every { ticketRepository.findOpenByLicensePlate(licensePlate = "ZUL0001") } returns parkedTicket
        every { spotRepository.findById(id = any()) } returns spot(id = spotId, occupied = true)
        val savedSpot = slot<Spot>()
        every { spotRepository.save(spot = capture(savedSpot)) } answers { savedSpot.captured }
        val savedTicket = slot<Ticket>()
        every { ticketRepository.save(ticket = capture(savedTicket)) } answers { savedTicket.captured }

        useCase.execute(
            command =
                VehicleEventCommand(
                    type = TicketEventType.EXIT,
                    licensePlate = "ZUL0001",
                    exitTime = LocalDateTime.parse("2025-01-01T14:00:00"),
                ),
        )

        assertFalse(savedSpot.captured.occupied) // spot was freed
        assertEquals(TicketStatus.CLOSED, savedTicket.captured.status)
        assertEquals(TicketEventType.EXIT, savedTicket.captured.events.last().type)
    }

    @Test
    fun `exit closes a ticket that never parked without touching any spot`() {
        every { ticketRepository.findOpenByLicensePlate(licensePlate = "ZUL0001") } returns
            Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime)
        val savedTicket = slot<Ticket>()
        every { ticketRepository.save(ticket = capture(savedTicket)) } answers { savedTicket.captured }

        useCase.execute(command = VehicleEventCommand(type = TicketEventType.EXIT, licensePlate = "ZUL0001"))

        assertEquals(TicketStatus.CLOSED, savedTicket.captured.status)
        verify(exactly = 0) { spotRepository.save(spot = any()) }
    }

    @Test
    fun `exit without an open ticket throws`() {
        every { ticketRepository.findOpenByLicensePlate(licensePlate = "ZUL0001") } returns null

        assertThrows<TicketException.OpenTicketNotFound> {
            useCase.execute(command = VehicleEventCommand(type = TicketEventType.EXIT, licensePlate = "ZUL0001"))
        }
    }
}
