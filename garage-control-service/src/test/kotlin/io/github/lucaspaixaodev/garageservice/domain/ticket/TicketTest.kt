package io.github.lucaspaixaodev.garageservice.domain.ticket

import io.github.lucaspaixaodev.garageservice.domain.Id
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.Money
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketCharge
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEvent
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEventTime
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEventType
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketStatus
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

class TicketTest {

    private val entryTime = LocalDateTime.parse("2025-01-01T12:00:00")
    private val hourlyPrice = Money.of(amount = "10.00")

    private fun parkedTicket(): Ticket =
        Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime).apply {
            park(spotId = Id.generate(), sector = GarageSector.A, hourlyPrice = hourlyPrice)
        }

    @Test
    fun `entry opens a ticket with an ENTRY event, no spot and no charge`() {
        val ticket = Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime)

        assertEquals("ZUL0001", ticket.vehicle.licensePlate)
        assertEquals(TicketStatus.OPEN, ticket.status)
        assertNull(ticket.spotId)
        assertNull(ticket.charge)
        assertEquals(1, ticket.events.size)
        assertEquals(TicketEventType.ENTRY, ticket.events.first().type)
        assertEquals(entryTime, ticket.events.first().time?.value)
    }

    @Test
    fun `park attaches the spot, snapshots the pricing and records a PARKED event`() {
        val spotId = Id.generate()
        val ticket = Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime)

        ticket.park(spotId = spotId, sector = GarageSector.A, hourlyPrice = hourlyPrice)

        assertEquals(spotId, ticket.spotId)
        assertEquals(GarageSector.A, ticket.charge?.sector)
        assertEquals(hourlyPrice, ticket.charge?.hourlyPrice)
        assertNull(ticket.charge?.fare)
        assertEquals(listOf(TicketEventType.ENTRY, TicketEventType.PARKED), ticket.events.map { it.type })
    }

    @Test
    fun `exit within the free window charges nothing`() {
        val ticket = parkedTicket()

        ticket.exit(exitTime = LocalDateTime.parse("2025-01-01T12:30:00"))

        assertEquals(TicketStatus.CLOSED, ticket.status)
        assertEquals(Money.ZERO, ticket.charge?.fare)
    }

    @Test
    fun `exit past the free window bills every started hour at the snapshot price`() {
        val ticket = parkedTicket()

        // 45 minutes -> 1 started hour
        ticket.exit(exitTime = LocalDateTime.parse("2025-01-01T12:45:00"))

        assertEquals(Money.of(amount = "10.00"), ticket.charge?.fare)
    }

    @Test
    fun `exit rounds the charged hours up`() {
        val ticket = parkedTicket()

        // 1h30 -> 2 started hours
        ticket.exit(exitTime = LocalDateTime.parse("2025-01-01T13:30:00"))

        assertEquals(Money.of(amount = "20.00"), ticket.charge?.fare)
    }

    @Test
    fun `exit without ever parking closes the ticket with no charge`() {
        val ticket = Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime)

        ticket.exit(exitTime = LocalDateTime.parse("2025-01-01T14:00:00"))

        assertEquals(TicketStatus.CLOSED, ticket.status)
        assertNull(ticket.charge)
        assertEquals(TicketEventType.EXIT, ticket.events.last().type)
    }

    @Test
    fun `restore rebuilds a ticket from stored state including its charge`() {
        val id = Id.generate()
        val spotId = Id.generate()
        val charge = TicketCharge(sector = GarageSector.B, hourlyPrice = hourlyPrice, fare = Money.of(amount = "20.00"))
        val ticket =
            Ticket.restore(
                id = id.toString(),
                licensePlate = "ZUL0001",
                spotId = spotId.toString(),
                status = TicketStatus.CLOSED,
                events = listOf(TicketEvent(type = TicketEventType.ENTRY, time = TicketEventTime(entryTime))),
                charge = charge,
            )

        assertEquals(id, ticket.id)
        assertEquals(spotId, ticket.spotId)
        assertEquals(TicketStatus.CLOSED, ticket.status)
        assertEquals(charge, ticket.charge)
        assertEquals(1, ticket.events.size)
    }
}
