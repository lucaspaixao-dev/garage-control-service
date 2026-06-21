package io.github.lucaspaixaodev.dashboard.api.stream

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/dashboard/stream")
class DashboardStreamController(
    private val broadcaster: DashboardEventBroadcaster
) {

    @GetMapping
    fun stream(): SseEmitter = broadcaster.subscribe()
}
