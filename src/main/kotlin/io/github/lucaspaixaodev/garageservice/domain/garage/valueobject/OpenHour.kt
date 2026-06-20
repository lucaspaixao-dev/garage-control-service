package io.github.lucaspaixaodev.garageservice.domain.garage.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.GarageException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@JvmInline
value class OpenHour(val value: LocalTime) {

    companion object {

        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun of(value: String): OpenHour =
            try {
                OpenHour(LocalTime.parse(value, FORMATTER))
            } catch (exception: DateTimeParseException) {
                throw GarageException.InvalidOpenHour(value = value, cause = exception)
            }
    }

    fun isAfter(closeHour: CloseHour): Boolean = value.isAfter(closeHour.value)

    override fun toString(): String = value.format(FORMATTER)
}
