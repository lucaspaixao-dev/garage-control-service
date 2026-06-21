package io.github.lucaspaixaodev.garageservice.application.ticket.usecase

import io.github.lucaspaixaodev.garageservice.application.garage.repository.GarageRepository
import io.github.lucaspaixaodev.garageservice.application.spot.repository.SpotRepository
import io.github.lucaspaixaodev.garageservice.application.ticket.repository.ProcessedEventRepository
import io.github.lucaspaixaodev.garageservice.application.ticket.repository.TicketRepository
import io.github.lucaspaixaodev.garageservice.domain.Id
import io.github.lucaspaixaodev.garageservice.domain.garage.Garage
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.Money
import io.github.lucaspaixaodev.garageservice.domain.spot.Spot
import io.github.lucaspaixaodev.garageservice.domain.ticket.Ticket
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketCharge
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEvent
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEventTime
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEventType
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class RegisterVehicleEventUseCaseTest {

    private val ticketRepository = mockk<TicketRepository>()
    private val spotRepository = mockk<SpotRepository>()
    private val garageRepository = mockk<GarageRepository>()
    private val processedEventRepository = mockk<ProcessedEventRepository>()
    private val useCase =
        RegisterVehicleEventUseCase(
            ticketRepository = ticketRepository,
            spotRepository = spotRepository,
            garageRepository = garageRepository,
            processedEventRepository = processedEventRepository,
        )

    private val entryTime = LocalDateTime.parse("2025-01-01T12:00:00")
    private val garageId = Id.generate()

    private fun firstTime() {
        every { processedEventRepository.register(eventId = any()) } returns true
    }

    private fun garage(basePrice: String): Garage =
        Garage.restore(
            id = garageId.toString(),
            sector = "A",
            basePrice = BigDecimal(basePrice),
            open = "00:00",
            close = "23:59",
            durationLimit = 1440,
        )

    private fun spot(occupied: Boolean): Spot =
        Spot.restore(
            id = Id.generate().toString(),
            externalId = 1,
            garageId = garageId.toString(),
            latitude = -23.561684,
            longitude = -46.655981,
            occupied = occupied,
        )

    private fun command(
        type: TicketEventType,
        latitude: Double? = null,
        longitude: Double? = null,
        exitTime: LocalDateTime? = null,
    ) = VehicleEventCommand(
        eventId = "evt-1",
        type = type,
        licensePlate = "ZUL0001",
        entryTime = entryTime,
        exitTime = exitTime,
        latitude = latitude,
        longitude = longitude,
    )

    @Test
    fun `a duplicate event is skipped without touching any repository`() {
        every { processedEventRepository.register(eventId = "evt-1") } returns false

        useCase.execute(command = command(type = TicketEventType.ENTRY))

        verify(exactly = 0) { ticketRepository.save(ticket = any()) }
        verify(exactly = 0) { ticketRepository.findOpenByLicensePlate(licensePlate = any()) }
    }

    @Test
    fun `entry opens and saves a new ticket when the garage has room`() {
        firstTime()
        every { spotRepository.countOccupied() } returns 4
        every { spotRepository.countTotal() } returns 10
        val saved = slot<Ticket>()
        every { ticketRepository.save(ticket = capture(saved)) } answers { saved.captured }

        useCase.execute(command = command(type = TicketEventType.ENTRY))

        assertEquals("ZUL0001", saved.captured.vehicle.licensePlate)
        assertEquals(TicketStatus.OPEN, saved.captured.status)
        verify(exactly = 1) { ticketRepository.save(ticket = any()) }
    }

    @Test
    fun `entry is skipped when the garage is full`() {
        firstTime()
        every { spotRepository.countOccupied() } returns 10
        every { spotRepository.countTotal() } returns 10

        useCase.execute(command = command(type = TicketEventType.ENTRY))

        verify(exactly = 0) { ticketRepository.save(ticket = any()) }
    }

    @Test
    fun `parked resolves the spot, prices it for the occupancy and attaches it to the open ticket`() {
        firstTime()
        every { ticketRepository.findOpenByLicensePlate(licensePlate = "ZUL0001") } returns
                Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime)
        every { spotRepository.findByCoordinates(latitude = -23.561684, longitude = -46.655981) } returns
                spot(occupied = false)
        every { garageRepository.findById(id = any()) } returns garage(basePrice = "10.00")
        // (4 + 1) / 10 = 50% occupancy -> base price
        every { spotRepository.countOccupied() } returns 4
        every { spotRepository.countTotal() } returns 10
        val savedSpot = slot<Spot>()
        every { spotRepository.save(spot = capture(savedSpot)) } answers { savedSpot.captured }
        val savedTicket = slot<Ticket>()
        every { ticketRepository.save(ticket = capture(savedTicket)) } answers { savedTicket.captured }

        useCase.execute(
            command = command(type = TicketEventType.PARKED, latitude = -23.561684, longitude = -46.655981),
        )

        assertTrue(savedSpot.captured.occupied)
        assertEquals(GarageSector.A, savedTicket.captured.charge?.sector)
        assertEquals(Money.of(amount = "10.00"), savedTicket.captured.charge?.hourlyPrice)
        assertEquals(TicketEventType.PARKED, savedTicket.captured.events.last().type)
    }

    @Test
    fun `parked without an open ticket is skipped`() {
        firstTime()
        every { ticketRepository.findOpenByLicensePlate(licensePlate = "ZUL0001") } returns null

        useCase.execute(
            command = command(type = TicketEventType.PARKED, latitude = -23.5, longitude = -46.6),
        )

        verify(exactly = 0) { spotRepository.save(spot = any()) }
        verify(exactly = 0) { ticketRepository.save(ticket = any()) }
    }

    @Test
    fun `parked with an unknown spot is skipped`() {
        firstTime()
        every { ticketRepository.findOpenByLicensePlate(licensePlate = "ZUL0001") } returns
                Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime)
        every { spotRepository.findByCoordinates(latitude = -23.5, longitude = -46.6) } returns null

        useCase.execute(
            command = command(type = TicketEventType.PARKED, latitude = -23.5, longitude = -46.6),
        )

        verify(exactly = 0) { spotRepository.save(spot = any()) }
        verify(exactly = 0) { ticketRepository.save(ticket = any()) }
    }

    @Test
    fun `parked on an already-occupied spot is skipped`() {
        firstTime()
        every { ticketRepository.findOpenByLicensePlate(licensePlate = "ZUL0001") } returns
                Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime)
        every { spotRepository.findByCoordinates(latitude = -23.561684, longitude = -46.655981) } returns
                spot(occupied = true)

        useCase.execute(
            command = command(type = TicketEventType.PARKED, latitude = -23.561684, longitude = -46.655981),
        )

        verify(exactly = 0) { spotRepository.save(spot = any()) }
        verify(exactly = 0) { ticketRepository.save(ticket = any()) }
    }

    @Test
    fun `parked when the garage is missing is skipped`() {
        firstTime()
        every { ticketRepository.findOpenByLicensePlate(licensePlate = "ZUL0001") } returns
                Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime)
        every { spotRepository.findByCoordinates(latitude = -23.561684, longitude = -46.655981) } returns
                spot(occupied = false)
        every { garageRepository.findById(id = any()) } returns null

        useCase.execute(
            command = command(type = TicketEventType.PARKED, latitude = -23.561684, longitude = -46.655981),
        )

        verify(exactly = 0) { spotRepository.save(spot = any()) }
        verify(exactly = 0) { ticketRepository.save(ticket = any()) }
    }

    @Test
    fun `exit releases the spot, closes the ticket and settles the fare`() {
        firstTime()
        val spotId = Id.generate()
        val parkedTicket =
            Ticket.restore(
                id = UUID.randomUUID().toString(),
                licensePlate = "ZUL0001",
                spotId = spotId.toString(),
                status = TicketStatus.OPEN,
                events = listOf(TicketEvent(type = TicketEventType.ENTRY, time = TicketEventTime(entryTime))),
                charge = TicketCharge(sector = GarageSector.A, hourlyPrice = Money.of(amount = "10.00")),
            )
        every { ticketRepository.findOpenByLicensePlate(licensePlate = "ZUL0001") } returns parkedTicket
        every { spotRepository.findById(id = any()) } returns spot(occupied = true)
        val savedSpot = slot<Spot>()
        every { spotRepository.save(spot = capture(savedSpot)) } answers { savedSpot.captured }
        val savedTicket = slot<Ticket>()
        every { ticketRepository.save(ticket = capture(savedTicket)) } answers { savedTicket.captured }

        useCase.execute(
            command = command(type = TicketEventType.EXIT, exitTime = LocalDateTime.parse("2025-01-01T14:00:00")),
        )

        assertFalse(savedSpot.captured.occupied)
        assertEquals(TicketStatus.CLOSED, savedTicket.captured.status)
        // 2 hours at 10.00
        assertEquals(Money.of(amount = "20.00"), savedTicket.captured.charge?.fare)
    }

    @Test
    fun `exit closes a ticket that never parked without touching any spot`() {
        firstTime()
        every { ticketRepository.findOpenByLicensePlate(licensePlate = "ZUL0001") } returns
                Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime)
        val savedTicket = slot<Ticket>()
        every { ticketRepository.save(ticket = capture(savedTicket)) } answers { savedTicket.captured }

        useCase.execute(
            command = command(type = TicketEventType.EXIT, exitTime = LocalDateTime.parse("2025-01-01T14:00:00")),
        )

        assertEquals(TicketStatus.CLOSED, savedTicket.captured.status)
        verify(exactly = 0) { spotRepository.save(spot = any()) }
    }

    @Test
    fun `exit without an open ticket is skipped`() {
        firstTime()
        every { ticketRepository.findOpenByLicensePlate(licensePlate = "ZUL0001") } returns null

        useCase.execute(
            command = command(type = TicketEventType.EXIT, exitTime = LocalDateTime.parse("2025-01-01T14:00:00")),
        )

        verify(exactly = 0) { ticketRepository.save(ticket = any()) }
    }
}
