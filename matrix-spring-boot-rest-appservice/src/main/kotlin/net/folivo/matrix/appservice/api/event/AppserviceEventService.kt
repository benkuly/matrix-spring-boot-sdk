package net.folivo.matrix.appservice.api.event

import net.folivo.matrix.core.model.events.Event

interface AppserviceEventService {

    enum class EventProcessingState {
        PROCESSED, NOT_PROCESSED
    }

    suspend fun eventProcessingState(tnxId: String, event: String): EventProcessingState
    suspend fun onEventProcessed(tnxId: String, eventId: String)

    suspend fun processEvent(event: Event<*>)
}