package io.github.lucaspaixaodev.dashboard.messaging

import io.awspring.cloud.sqs.annotation.SqsListener
import io.github.lucaspaixaodev.dashboard.api.stream.DashboardEventBroadcaster
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DashboardEventConsumer(
    private val broadcaster: DashboardEventBroadcaster
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(DashboardEventConsumer::class.java)
    }

    @SqsListener($$"${dashboard.events.queue-name}")
    fun onMessage(message: DashboardEventMessage) {
        logger.info("Streaming dashboard event type=${message.type} plate=${message.licensePlate}")
        broadcaster.broadcast(message)
    }
}

data class DashboardEventMessage(
    val type: String,
    val licensePlate: String,
    val at: String,
)
