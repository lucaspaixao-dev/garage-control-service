package io.github.lucaspaixaodev.garageservice.domain.spot.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.SpotException

@JvmInline
value class Longitude(val value: Double) {

    init {
        if (value !in MIN..MAX) throw SpotException.InvalidLongitude(value = value, min = MIN, max = MAX)
    }

    companion object {

        private const val MIN = -180.0
        private const val MAX = 180.0

        fun of(value: Double): Longitude = Longitude(value)
    }

    override fun toString(): String = value.toString()
}
