package io.github.lucaspaixaodev.garageservice.domain.ticket

import io.github.lucaspaixaodev.garageservice.domain.Id
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TicketTest {

    private val entryTime = LocalDateTime.parse("2025-01-01T12:00:00")

    @Test
    fun `entry opens a ticket with an ENTRY event and no spot`() {
        val ticket = Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime)

        assertEquals("ZUL0001", ticket.vehicle.licensePlate)
        assertEquals(TicketStatus.OPEN, ticket.status)
        assertNull(ticket.spotId)
        assertEquals(1, ticket.events.size)
        assertEquals(TicketEventType.ENTRY, ticket.events.first().type)
        assertEquals(entryTime, ticket.events.first().time?.value)
    }

    @Test
    fun `park attaches the spot and records a PARKED event`() {
        val ticket = Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime)
        val spotId = Id.generate()

        ticket.park(spotId = spotId)

        assertEquals(spotId, ticket.spotId)
        assertEquals(listOf(TicketEventType.ENTRY, TicketEventType.PARKED), ticket.events.map { it.type })
        assertNull(ticket.events.last().time)
    }

    @Test
    fun `exit records an EXIT event and closes the ticket`() {
        val ticket = Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime)
        val exitTime = LocalDateTime.parse("2025-01-01T14:00:00")

        ticket.exit(exitTime = exitTime)

        assertEquals(TicketStatus.CLOSED, ticket.status)
        assertEquals(TicketEventType.EXIT, ticket.events.last().type)
        assertEquals(exitTime, ticket.events.last().time?.value)
        ticket.getFare() // currently a no-op; exercised for coverage
    }

    @Test
    fun `restore rebuilds a ticket from stored state`() {
        val id = Id.generate()
        val spotId = Id.generate()
        val ticket =
            Ticket.restore(
                id = id.toString(),
                licensePlate = "ZUL0001",
                spotId = spotId.toString(),
                status = TicketStatus.CLOSED,
                events = listOf(TicketEvent(type = TicketEventType.ENTRY, time = TicketEventTime(entryTime))),
            )

        assertEquals(id, ticket.id)
        assertEquals(spotId, ticket.spotId)
        assertEquals(TicketStatus.CLOSED, ticket.status)
        assertEquals(1, ticket.events.size)
    }
}
