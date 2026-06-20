package io.github.lucaspaixaodev.garageservice.domain

import java.util.UUID

@JvmInline
value class Id(val value: UUID) {

    companion object {

        fun generate(): Id = Id(UUID.randomUUID())

        fun of(value: String): Id = Id(UUID.fromString(value))
    }

    override fun toString(): String = value.toString()
}