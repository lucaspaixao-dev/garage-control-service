package io.github.lucaspaixaodev.garageservice.domain.spot.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.SpotException

@JvmInline
value class Latitude(val value: Double) {

    init {
        if (value !in MIN..MAX) throw SpotException.InvalidLatitude(value, MIN, MAX)
    }

    companion object {

        private const val MIN = -90.0
        private const val MAX = 90.0

        fun of(value: Double): Latitude = Latitude(value)
    }

    override fun toString(): String = value.toString()
}
