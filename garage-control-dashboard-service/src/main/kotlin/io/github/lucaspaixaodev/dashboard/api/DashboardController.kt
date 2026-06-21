package io.github.lucaspaixaodev.dashboard.api

import io.github.lucaspaixaodev.dashboard.query.DashboardQueryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/dashboard")
class DashboardController(
    private val dashboardQueryService: DashboardQueryService
) {

    @GetMapping
    fun snapshot(): ResponseEntity<DashboardView> = ResponseEntity.ok(dashboardQueryService.snapshot())
}
