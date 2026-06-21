package io.github.lucaspaixaodev.dashboard.api

import io.github.lucaspaixaodev.dashboard.query.DashboardQueryService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

class DashboardControllerTest {

    private val dashboardQueryService = mockk<DashboardQueryService>()
    private val controller = DashboardController(dashboardQueryService = dashboardQueryService)

    @Test
    fun `snapshot returns 200 with the dashboard view`() {
        val view =
            DashboardView(
                summary =
                    SummaryView(
                        totalSpots = 30,
                        occupiedSpots = 12,
                        freeSpots = 18,
                        occupancyRate = 0.4,
                        revenueBySector = emptyList(),
                    ),
                spots = emptyList(),
                tickets = emptyList(),
            )
        every { dashboardQueryService.snapshot() } returns view

        val response = controller.snapshot()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(30, response.body!!.summary.totalSpots)
    }
}
