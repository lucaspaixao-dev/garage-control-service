package io.github.lucaspaixaodev.garageservice.domain.exception

sealed class GarageApiException(
    message: String,
    cause: Throwable? = null,
) : BaseException(message, cause) {

    class EmptyResponse : GarageApiException("Garage API returned an empty response")

    class FetchFailed(
        cause: Throwable,
    ) : GarageApiException("Failed to fetch data from the garage API: ${cause.message ?: "unknown error"}", cause)
}
