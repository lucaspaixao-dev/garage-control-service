package io.github.lucaspaixaodev.garageservice.infra.output.messaging

import io.awspring.cloud.sqs.operations.SqsTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * Publishes already-processed vehicle events to the dashboard queue so the
 * dashboard service can stream them to browsers. Best-effort: a dashboard/SQS
 * outage must never break core event processing.
 */
@Component
class DashboardEventPublisher(
    private val sqsTemplate: SqsTemplate,
    @Value($$"${garage.dashboard.queue-name}") private val queueName: String,
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(DashboardEventPublisher::class.java)
    }

    fun publish(type: String, licensePlate: String) {
        runCatching {
            sqsTemplate.send { options ->
                options
                    .queue(queueName)
                    .payload(
                        DashboardEventMessage(
                            type = type,
                            licensePlate = licensePlate,
                            at = LocalDateTime.now().toString(),
                        ),
                    )
            }
        }.onFailure { error ->
            logger.warn("Failed to publish dashboard event type=$type plate=$licensePlate: ${error.message}")
        }
    }
}

data class DashboardEventMessage(
    val type: String,
    val licensePlate: String,
    val at: String,
)
