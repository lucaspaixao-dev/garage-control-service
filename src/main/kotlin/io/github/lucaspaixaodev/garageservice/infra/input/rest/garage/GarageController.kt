package io.github.lucaspaixaodev.garageservice.infra.input.rest.garage

import io.github.lucaspaixaodev.garageservice.application.garage.usecase.SetupGarageUseCase
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// TODO: We need to remove this endpoint and migrate to startup project, to boot the garages and spots.
@RestController
@RequestMapping("/garages")
class GarageController(
    private val setupGarageUseCase: SetupGarageUseCase
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(GarageController::class.java)
    }

    @PostMapping
    fun setup(): ResponseEntity<SetupGarageResponse> {
        logger.info("POST /garages")

        val result = setupGarageUseCase.execute()

        logger.info("Garage setup completed garages=${result.garages} spots=${result.spots}")

        val body = SetupGarageResponse(garages = result.garages, spots = result.spots)
        return ResponseEntity.status(HttpStatus.CREATED).body(body)
    }
}

data class SetupGarageResponse(
    val garages: Int,
    val spots: Int
)
