package io.github.lucaspaixaodev.dashboard.messaging

import io.github.lucaspaixaodev.dashboard.api.stream.DashboardEventBroadcaster
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class DashboardEventConsumerTest {

    private val broadcaster = mockk<DashboardEventBroadcaster>(relaxed = true)
    private val consumer = DashboardEventConsumer(broadcaster = broadcaster)

    @Test
    fun `forwards the consumed event to the SSE broadcaster`() {
        val message = DashboardEventMessage(type = "PARKED", licensePlate = "ZUL0001", at = "2025-01-01T12:00:00")

        consumer.onMessage(message = message)

        verify(exactly = 1) { broadcaster.broadcast(event = message) }
    }
}
