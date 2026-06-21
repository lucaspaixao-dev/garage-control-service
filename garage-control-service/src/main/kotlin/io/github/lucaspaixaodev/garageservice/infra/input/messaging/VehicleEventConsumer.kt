package io.github.lucaspaixaodev.garageservice.infra.input.messaging

import io.awspring.cloud.sqs.annotation.SqsListener
import io.github.lucaspaixaodev.garageservice.application.ticket.usecase.RegisterVehicleEventUseCase
import io.github.lucaspaixaodev.garageservice.application.ticket.usecase.VehicleEventCommand
import io.github.lucaspaixaodev.garageservice.domain.ticket.TicketEventType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class VehicleEventConsumer(
    private val registerVehicleEvent: RegisterVehicleEventUseCase
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(VehicleEventConsumer::class.java)
    }

    @SqsListener($$"${garage.events.queue-name}")
    fun onMessage(message: VehicleEventMessage) {
        logger.info("Received vehicle event plate=${message.licensePlate} type=${message.eventType}")

        registerVehicleEvent.execute(command = message.toCommand())

        logger.info("Processed vehicle event plate=${message.licensePlate} type=${message.eventType}")
    }

    private fun VehicleEventMessage.toCommand(): VehicleEventCommand =
        VehicleEventCommand(
            type = TicketEventType.valueOf(eventType),
            licensePlate = licensePlate,
            entryTime = entryTime?.toLocalDateTime(),
            exitTime = exitTime?.toLocalDateTime(),
            latitude = lat,
            longitude = lng,
        )

    // The garage simulator sends ISO date-times that may or may not carry an offset
    // (e.g. "2026-06-21T09:09:09" or "2026-06-21T09:09:09Z"); ISO_DATE_TIME accepts both.
    private fun String.toLocalDateTime(): LocalDateTime =
        LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
}

data class VehicleEventMessage(
    val id: String,
    val licensePlate: String,
    val eventType: String,
    val entryTime: String? = null,
    val exitTime: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val receivedAt: String? = null,
)
