package io.github.lucaspaixaodev.webhook.messaging

import io.awspring.cloud.sqs.operations.SqsTemplate
import io.github.lucaspaixaodev.webhook.persistence.VehicleEventItem
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class VehicleEventPublisher(
    private val sqsTemplate: SqsTemplate,
    @Value($$"${webhook.sqs.queue-name}") private val queueName: String,
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(VehicleEventPublisher::class.java)
    }

    fun publish(event: VehicleEventItem) {
        logger.info("Publishing vehicle event id=${event.id} plate=${event.licensePlate} to SQS queue=$queueName")

        val message =
            VehicleEventMessage(
                id = event.id,
                licensePlate = event.licensePlate,
                eventType = event.eventType,
                entryTime = event.entryTime,
                exitTime = event.exitTime,
                lat = event.lat,
                lng = event.lng,
                receivedAt = event.receivedAt,
            )
        sqsTemplate.send { options ->
            options
                .queue(queueName)
                .payload(message)
                // One vehicle's events (entry -> parked -> exit) stay strictly ordered.
                .messageGroupId(event.licensePlate)
                // The event id is unique, so it doubles as the dedup key.
                .messageDeduplicationId(event.id)
        }

        logger.info("Published vehicle event id=${event.id} to SQS")
    }
}
