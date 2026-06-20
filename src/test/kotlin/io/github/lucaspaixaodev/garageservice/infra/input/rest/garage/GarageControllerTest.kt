package io.github.lucaspaixaodev.garageservice.infra.input.rest.garage

import io.github.lucaspaixaodev.garageservice.application.garage.usecase.SetupGarageResult
import io.github.lucaspaixaodev.garageservice.application.garage.usecase.SetupGarageUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

class GarageControllerTest {

    private val setupGarageUseCase = mockk<SetupGarageUseCase>()
    private val controller = GarageController(setupGarageUseCase = setupGarageUseCase)

    @Test
    fun `setup runs the use case and returns 201 with the counts`() {
        every { setupGarageUseCase.execute() } returns SetupGarageResult(garages = 2, spots = 30)

        val response = controller.setup()

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(2, response.body!!.garages)
        assertEquals(30, response.body!!.spots)
        verify(exactly = 1) { setupGarageUseCase.execute() }
    }
}
