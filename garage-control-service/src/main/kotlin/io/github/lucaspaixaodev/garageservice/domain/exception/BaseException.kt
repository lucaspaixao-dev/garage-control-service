package io.github.lucaspaixaodev.garageservice.domain.exception

sealed class BaseException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)