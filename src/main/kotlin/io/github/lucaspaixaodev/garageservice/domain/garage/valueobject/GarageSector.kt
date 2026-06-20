package io.github.lucaspaixaodev.garageservice.domain.garage.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.GarageException

enum class GarageSector {
    A,
    B;

    companion object {

        fun of(value: String): GarageSector =
            entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: throw GarageException.InvalidSector(value, entries.joinToString { it.name })
    }
}