package io.github.lucaspaixaodev.garageservice.application.garage.gateway

import java.math.BigDecimal

interface GarageGateway {

    fun fetch(): GarageData
}

data class GarageData(
    val garages: List<GarageInfo>,
    val spots: List<SpotInfo>
)

data class GarageInfo(
    val sector: String,
    val basePrice: BigDecimal,
    val openHour: String,
    val closeHour: String,
    val durationLimitMinutes: Int
)

data class SpotInfo(
    val externalId: Int,
    val sector: String,
    val latitude: Double,
    val longitude: Double,
    val occupied: Boolean
)
