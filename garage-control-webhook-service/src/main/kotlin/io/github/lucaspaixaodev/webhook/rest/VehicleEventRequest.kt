package io.github.lucaspaixaodev.webhook.rest

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Single payload that accepts the three garage events. Fields specific to one
 * event type are nullable: ENTRY carries [entryTime], PARKED carries [lat]/[lng],
 * EXIT carries [exitTime].
 */
data class VehicleEventRequest(
    @JsonProperty("license_plate") val licensePlate: String,
    @JsonProperty("event_type") val eventType: VehicleEventType,
    @JsonProperty("entry_time") val entryTime: String? = null,
    @JsonProperty("exit_time") val exitTime: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)
