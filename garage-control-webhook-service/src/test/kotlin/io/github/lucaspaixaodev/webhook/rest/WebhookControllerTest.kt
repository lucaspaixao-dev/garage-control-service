package io.github.lucaspaixaodev.webhook.rest

import io.github.lucaspaixaodev.webhook.messaging.VehicleEventPublisher
import io.github.lucaspaixaodev.webhook.persistence.VehicleEventItem
import io.github.lucaspaixaodev.webhook.persistence.VehicleEventRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class WebhookControllerTest {

    private val vehicleEventRepository = mockk<VehicleEventRepository>()
    private val vehicleEventPublisher = mockk<VehicleEventPublisher>()
    private val controller =
        WebhookController(
            vehicleEventRepository = vehicleEventRepository,
            vehicleEventPublisher = vehicleEventPublisher,
        )

    private val stored = slot<VehicleEventItem>()
    private val published = slot<VehicleEventItem>()

    private fun stubSaveAndPublish() {
        every { vehicleEventRepository.save(event = capture(stored)) } just Runs
        every { vehicleEventPublisher.publish(event = capture(published)) } just Runs
    }

    @Test
    fun `entry event is stored, published and answered with 200`() {
        stubSaveAndPublish()

        val response =
            controller.receive(
                request =
                    VehicleEventRequest(
                        licensePlate = "ZUL0001",
                        eventType = VehicleEventType.ENTRY,
                        entryTime = "2025-01-01T12:00:00.000Z",
                    ),
            )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("ZUL0001", stored.captured.licensePlate)
        assertEquals("ENTRY", stored.captured.eventType)
        assertEquals("2025-01-01T12:00:00.000Z", stored.captured.entryTime)
        assertNull(stored.captured.lat)
        assertTrue(stored.captured.id.isNotBlank())
        assertTrue(stored.captured.receivedAt.isNotBlank())
        // the same event is both stored and published
        assertEquals(stored.captured.id, published.captured.id)
        verify(exactly = 1) { vehicleEventRepository.save(event = any()) }
        verify(exactly = 1) { vehicleEventPublisher.publish(event = any()) }
    }

    @Test
    fun `parked event carries the coordinates`() {
        stubSaveAndPublish()

        controller.receive(
            request =
                VehicleEventRequest(
                    licensePlate = "ZUL0001",
                    eventType = VehicleEventType.PARKED,
                    lat = -23.561684,
                    lng = -46.655981,
                ),
        )

        assertEquals("PARKED", stored.captured.eventType)
        assertEquals(-23.561684, stored.captured.lat)
        assertEquals(-46.655981, stored.captured.lng)
        assertNull(stored.captured.entryTime)
    }

    @Test
    fun `exit event carries the exit time`() {
        stubSaveAndPublish()

        controller.receive(
            request =
                VehicleEventRequest(
                    licensePlate = "ZUL0001",
                    eventType = VehicleEventType.EXIT,
                    exitTime = "2025-01-01T12:00:00.000Z",
                ),
        )

        assertEquals("EXIT", stored.captured.eventType)
        assertEquals("2025-01-01T12:00:00.000Z", stored.captured.exitTime)
    }
}
