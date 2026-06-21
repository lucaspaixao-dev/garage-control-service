package io.github.lucaspaixaodev.garageservice.infra.output.gateway.garage

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.lucaspaixaodev.garageservice.application.garage.gateway.GarageData
import io.github.lucaspaixaodev.garageservice.application.garage.gateway.GarageGateway
import io.github.lucaspaixaodev.garageservice.application.garage.gateway.GarageInfo
import io.github.lucaspaixaodev.garageservice.application.garage.gateway.SpotInfo
import io.github.lucaspaixaodev.garageservice.domain.exception.GarageApiException
import java.math.BigDecimal
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@Component
class GarageClient(
    restClientBuilder: RestClient.Builder,
    @Value($$"${garage.api.base-url}") baseUrl: String
) : GarageGateway {

    private companion object {
        private val logger = LoggerFactory.getLogger(GarageClient::class.java)
    }

    private val restClient: RestClient = restClientBuilder
        .baseUrl(baseUrl)
        .build()

    override fun fetch(): GarageData {
        logger.info("Fetching garage data from garage API GET /garage")

        val response =
            try {
                restClient.get()
                    .uri("/garage")
                    .retrieve()
                    .body(GarageResponse::class.java)
            } catch (exception: RestClientException) {
                throw GarageApiException.FetchFailed(exception)
            } ?: throw GarageApiException.EmptyResponse()

        logger.info("Fetched garages=${response.garage.size} spots=${response.spots.size} from garage API")

        return GarageData(
            garages = response.garage.map { it.toModel() },
            spots = response.spots.map { it.toModel() }
        )
    }

    private fun GarageResponse.GarageItem.toModel(): GarageInfo =
        GarageInfo(
            sector = sector,
            basePrice = basePrice,
            openHour = openHour,
            closeHour = closeHour,
            durationLimitMinutes = durationLimitMinutes
        )

    private fun GarageResponse.SpotItem.toModel(): SpotInfo =
        SpotInfo(
            externalId = id,
            sector = sector,
            latitude = lat,
            longitude = lng,
            occupied = occupied
        )
}

data class GarageResponse(
    val garage: List<GarageItem>,
    val spots: List<SpotItem>
) {

    data class GarageItem(
        val sector: String,
        @JsonProperty("base_price") val basePrice: BigDecimal,
        @JsonProperty("max_capacity") val maxCapacity: Int,
        @JsonProperty("open_hour") val openHour: String,
        @JsonProperty("close_hour") val closeHour: String,
        @JsonProperty("duration_limit_minutes") val durationLimitMinutes: Int
    )

    data class SpotItem(
        val id: Int,
        val sector: String,
        val lat: Double,
        val lng: Double,
        val occupied: Boolean
    )
}
