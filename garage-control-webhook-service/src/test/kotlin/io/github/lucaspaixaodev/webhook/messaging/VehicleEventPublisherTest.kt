package io.github.lucaspaixaodev.webhook.messaging

import io.awspring.cloud.sqs.operations.SqsSendOptions
import io.awspring.cloud.sqs.operations.SqsTemplate
import io.github.lucaspaixaodev.webhook.persistence.VehicleEventItem
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.function.Consumer
import org.junit.jupiter.api.Test

class VehicleEventPublisherTest {

    private val sqsTemplate = mockk<SqsTemplate>()
    private val publisher = VehicleEventPublisher(sqsTemplate = sqsTemplate, queueName = "vehicle-events.fifo")

    @Test
    fun `publish sends the event to the fifo queue grouped by license plate`() {
        // self-returning options mock so the whole fluent chain runs and is verifiable
        val options = mockk<SqsSendOptions<VehicleEventMessage>>()
        every { options.queue(any()) } returns options
        every { options.payload(any()) } returns options
        every { options.messageGroupId(any()) } returns options
        every { options.messageDeduplicationId(any()) } returns options
        every { sqsTemplate.send(any<Consumer<SqsSendOptions<VehicleEventMessage>>>()) } answers {
            firstArg<Consumer<SqsSendOptions<VehicleEventMessage>>>().accept(options)
            mockk(relaxed = true)
        }
        val event =
            VehicleEventItem().apply {
                id = "evt-1"
                licensePlate = "ZUL0001"
                eventType = "ENTRY"
                entryTime = "2025-01-01T12:00:00.000Z"
                receivedAt = "2026-06-20T10:00:00Z"
            }

        publisher.publish(event = event)

        verify(exactly = 1) { sqsTemplate.send(any<Consumer<SqsSendOptions<VehicleEventMessage>>>()) }
        verify { options.queue("vehicle-events.fifo") }
        verify {
            options.payload(
                VehicleEventMessage(
                    id = "evt-1",
                    licensePlate = "ZUL0001",
                    eventType = "ENTRY",
                    entryTime = "2025-01-01T12:00:00.000Z",
                    exitTime = null,
                    lat = null,
                    lng = null,
                    receivedAt = "2026-06-20T10:00:00Z",
                ),
            )
        }
        // FIFO ordering key is the license plate
        verify { options.messageGroupId("ZUL0001") }
        verify { options.messageDeduplicationId("evt-1") }
    }
}
