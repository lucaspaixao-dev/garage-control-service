package io.github.lucaspaixaodev.garageservice.infra.input.rest

import io.github.lucaspaixaodev.garageservice.domain.exception.BaseException
import io.github.lucaspaixaodev.garageservice.domain.exception.GarageApiException
import io.github.lucaspaixaodev.garageservice.domain.exception.GarageException
import io.github.lucaspaixaodev.garageservice.domain.exception.MoneyException
import io.github.lucaspaixaodev.garageservice.domain.exception.SpotException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(
    val message: String,
)

@RestControllerAdvice
class DomainExceptionHandler {

    private val logger = LoggerFactory.getLogger(DomainExceptionHandler::class.java)

    @ExceptionHandler(BaseException::class)
    fun handleDomain(exception: BaseException): ResponseEntity<ErrorResponse> {
        val status =
            when (exception) {
                is GarageApiException -> HttpStatus.BAD_GATEWAY
                is GarageException -> HttpStatus.UNPROCESSABLE_ENTITY
                is SpotException -> HttpStatus.UNPROCESSABLE_ENTITY
                is MoneyException -> HttpStatus.UNPROCESSABLE_ENTITY
            }

        if (status.is5xxServerError) {
            logger.error("${exception.javaClass.simpleName}: ${exception.message}", exception)
        } else {
            logger.warn("${exception.javaClass.simpleName}: ${exception.message}")
        }

        return ResponseEntity
            .status(status)
            .body(ErrorResponse(message = exception.message ?: "An unexpected error occurred"))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(exception: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error: ${exception.message}", exception)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(message = "An unexpected error occurred"))
    }
}
