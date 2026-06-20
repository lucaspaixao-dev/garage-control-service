package io.github.lucaspaixaodev.webhook.messaging

data class VehicleEventMessage(
    val id: String,
    val licensePlate: String,
    val eventType: String,
    val entryTime: String?,
    val exitTime: String?,
    val lat: Double?,
    val lng: Double?,
    val receivedAt: String,
)
