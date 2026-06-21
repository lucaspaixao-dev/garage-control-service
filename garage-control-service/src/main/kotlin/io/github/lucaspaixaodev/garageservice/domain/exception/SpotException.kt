package io.github.lucaspaixaodev.garageservice.domain.exception

sealed class SpotException(
    message: String,
) : BaseException(message) {

    class InvalidLatitude(
        value: Double,
        min: Double,
        max: Double,
    ) : SpotException("Latitude must be between $min and $max degrees, got: $value")

    class InvalidLongitude(
        value: Double,
        min: Double,
        max: Double,
    ) : SpotException("Longitude must be between $min and $max degrees, got: $value")

    class InvalidExternalId(
        value: Int,
    ) : SpotException("Spot external id must be greater than 0, got: $value")

    class GarageNotFoundForSector(
        externalId: Int,
        sector: String,
    ) : SpotException("Spot '$externalId' references sector '$sector' that has no garage")
}
