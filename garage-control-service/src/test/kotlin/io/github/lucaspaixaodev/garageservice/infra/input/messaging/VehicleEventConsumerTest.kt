package io.github.lucaspaixaodev.garageservice.infra.input.messaging

import io.github.lucaspaixaodev.garageservice.application.ticket.usecase.RegisterVehicleEventUseCase
import io.github.lucaspaixaodev.garageservice.application.ticket.usecase.VehicleEventCommand
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEventType
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

class VehicleEventConsumerTest {

    private val registerVehicleEvent = mockk<RegisterVehicleEventUseCase>()
    private val consumer = VehicleEventConsumer(registerVehicleEvent = registerVehicleEvent)

    @Test
    fun `maps an ENTRY message to a command parsing the ISO entry time`() {
        val command = slot<VehicleEventCommand>()
        every { registerVehicleEvent.execute(command = capture(command)) } just Runs

        consumer.onMessage(
            message =
                VehicleEventMessage(
                    id = "evt-1",
                    licensePlate = "ZUL0001",
                    eventType = "ENTRY",
                    entryTime = "2025-01-01T12:00:00",
                ),
        )

        assertEquals(TicketEventType.ENTRY, command.captured.type)
        assertEquals("ZUL0001", command.captured.licensePlate)
        assertEquals(LocalDateTime.parse("2025-01-01T12:00:00"), command.captured.entryTime)
        assertNull(command.captured.latitude)
        verify(exactly = 1) { registerVehicleEvent.execute(command = any()) }
    }

    @Test
    fun `maps a PARKED message to a command carrying the coordinates`() {
        val command = slot<VehicleEventCommand>()
        every { registerVehicleEvent.execute(command = capture(command)) } just Runs

        consumer.onMessage(
            message =
                VehicleEventMessage(
                    id = "evt-2",
                    licensePlate = "ZUL0001",
                    eventType = "PARKED",
                    lat = -23.561684,
                    lng = -46.655981,
                ),
        )

        assertEquals(TicketEventType.PARKED, command.captured.type)
        assertEquals(-23.561684, command.captured.latitude)
        assertEquals(-46.655981, command.captured.longitude)
        assertNull(command.captured.entryTime)
    }
}
