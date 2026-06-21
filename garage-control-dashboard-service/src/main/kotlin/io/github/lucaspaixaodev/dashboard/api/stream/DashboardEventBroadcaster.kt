package io.github.lucaspaixaodev.dashboard.api.stream

import io.github.lucaspaixaodev.dashboard.messaging.DashboardEventMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CopyOnWriteArrayList

/** Fans out vehicle events consumed from the queue to every connected browser via SSE. */
@Component
class DashboardEventBroadcaster {

    private companion object {
        private val logger = LoggerFactory.getLogger(DashboardEventBroadcaster::class.java)
        private const val EVENT_NAME = "vehicle-event"
    }

    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    fun subscribe(): SseEmitter {
        val emitter = SseEmitter(Long.MAX_VALUE)
        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }
        emitters.add(emitter)
        logger.info("Dashboard SSE client subscribed (total={})", emitters.size)
        return emitter
    }

    fun broadcast(event: DashboardEventMessage) {
        emitters.forEach { emitter ->
            runCatching { emitter.send(SseEmitter.event().name(EVENT_NAME).data(event)) }
                .onFailure { emitters.remove(emitter) }
        }
    }
}
