package net.folivo.matrix.appservice.api.event

import net.folivo.matrix.core.model.events.Event
import reactor.core.publisher.Mono

interface MatrixAppserviceEventService {

    enum class EventProcessingState {
        PROCESSED, NOT_PROCESSED
    }

    fun eventProcessingState(tnxId: String, eventIdOrType: String): Mono<EventProcessingState>
    fun saveEventProcessed(tnxId: String, eventIdOrType: String): Mono<Void>

    fun processEvent(event: Event<*>): Mono<Void>
}