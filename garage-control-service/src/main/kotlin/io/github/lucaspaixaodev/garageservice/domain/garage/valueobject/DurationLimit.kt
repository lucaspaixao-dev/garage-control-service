package io.github.lucaspaixaodev.garageservice.domain.garage.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.GarageException
import java.time.Duration

@JvmInline
value class DurationLimit(val minutes: Int) {

    init {
        if (minutes <= 0) throw GarageException.InvalidDurationLimit(minutes)
    }

    val duration: Duration
        get() = Duration.ofMinutes(minutes.toLong())

    companion object {

        fun ofMinutes(minutes: Int): DurationLimit = DurationLimit(minutes)
    }

    override fun toString(): String = "$minutes min"
}
