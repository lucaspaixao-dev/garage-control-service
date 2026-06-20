package io.github.lucaspaixaodev.garageservice.domain.exception

sealed class GarageException(
    message: String,
    cause: Throwable? = null,
) : BaseException(message, cause) {

    class InvalidSector(
        value: String,
        allowed: String,
    ) : GarageException("Invalid garage sector '$value'. Allowed values: $allowed")

    class InvalidDurationLimit(
        minutes: Int,
    ) : GarageException("Duration limit must be greater than 0 minutes, got: $minutes")

    class InvalidOpenHour(
        value: String,
        cause: Throwable? = null,
    ) : GarageException("Invalid open hour '$value': expected format HH:mm", cause)

    class InvalidCloseHour(
        value: String,
        cause: Throwable? = null,
    ) : GarageException("Invalid close hour '$value': expected format HH:mm", cause)

    class CloseHourBeforeOpenHour(
        openHour: String,
        closeHour: String,
    ) : GarageException("Close hour '$closeHour' must not be before open hour '$openHour'")
}
