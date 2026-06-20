package io.github.lucaspaixaodev.garageservice.domain.garage.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.GarageException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@JvmInline
value class CloseHour(val value: LocalTime) {

    companion object {

        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun of(value: String): CloseHour =
            try {
                CloseHour(LocalTime.parse(value, FORMATTER))
            } catch (exception: DateTimeParseException) {
                throw GarageException.InvalidCloseHour(value = value, cause = exception)
            }
    }

    fun isBefore(openHour: OpenHour): Boolean = value.isBefore(openHour.value)

    override fun toString(): String = value.format(FORMATTER)
}
