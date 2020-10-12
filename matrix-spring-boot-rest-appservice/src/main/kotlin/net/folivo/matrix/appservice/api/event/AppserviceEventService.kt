package net.folivo.matrix.appservice.api.event

import net.folivo.matrix.core.model.events.Event

interface AppserviceEventService {

    enum class EventProcessingState {
        PROCESSED, NOT_PROCESSED
    }

    suspend fun eventProcessingState(tnxId: String, eventIdOrHash: String): EventProcessingState
    suspend fun onEventProcessed(tnxId: String, eventIdOrHash: String)

    suspend fun processEvent(event: Event<*>)
}