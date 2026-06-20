package io.github.lucaspaixaodev.garageservice.domain.spot.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.SpotException

@JvmInline
value class ExternalId(val value: Int) {

    init {
        if (value <= 0) throw SpotException.InvalidExternalId(value)
    }

    companion object {

        fun of(value: Int): ExternalId = ExternalId(value)
    }

    override fun toString(): String = value.toString()
}
