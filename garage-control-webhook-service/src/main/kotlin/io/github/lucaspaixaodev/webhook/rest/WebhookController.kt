package io.github.lucaspaixaodev.webhook.rest

import io.github.lucaspaixaodev.webhook.messaging.VehicleEventPublisher
import io.github.lucaspaixaodev.webhook.persistence.VehicleEventItem
import io.github.lucaspaixaodev.webhook.persistence.VehicleEventRepository
import java.time.Instant
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class WebhookController(
    private val vehicleEventRepository: VehicleEventRepository,
    private val vehicleEventPublisher: VehicleEventPublisher,
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(WebhookController::class.java)
    }

    @PostMapping("/webhook")
    fun receive(
        @RequestBody request: VehicleEventRequest,
    ): ResponseEntity<Void> {
        logger.info("POST /webhook plate=${request.licensePlate} eventType=${request.eventType}")

        val event = request.toItem()
        vehicleEventRepository.save(event = event)
        vehicleEventPublisher.publish(event = event)

        logger.info("Vehicle event accepted id=${event.id}")
        return ResponseEntity.ok().build()
    }

    private fun VehicleEventRequest.toItem(): VehicleEventItem =
        VehicleEventItem().apply {
            id = UUID.randomUUID().toString()
            licensePlate = this@toItem.licensePlate
            eventType = this@toItem.eventType.name
            entryTime = this@toItem.entryTime
            exitTime = this@toItem.exitTime
            lat = this@toItem.lat
            lng = this@toItem.lng
            receivedAt = Instant.now().toString()
        }
}
