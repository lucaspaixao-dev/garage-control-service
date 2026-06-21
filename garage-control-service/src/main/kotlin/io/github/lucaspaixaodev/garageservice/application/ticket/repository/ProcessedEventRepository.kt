package io.github.lucaspaixaodev.garageservice.application.ticket.repository

/**
 * Inbox/dedup store that makes event handling idempotent: the same vehicle event
 * (identified by the webhook id) is only ever processed once, even if the queue
 * redelivers it.
 */
interface ProcessedEventRepository {

    /**
     * Registers the event id within the current transaction.
     * Returns `true` if it was seen for the first time (caller should process it)
     * or `false` if it was already processed (caller should skip).
     */
    fun register(eventId: String): Boolean
}
