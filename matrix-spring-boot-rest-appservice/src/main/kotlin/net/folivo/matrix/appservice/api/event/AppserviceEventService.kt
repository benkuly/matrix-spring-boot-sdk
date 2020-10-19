package net.folivo.matrix.appservice.api.event

import net.folivo.matrix.core.model.MatrixId.EventId
import net.folivo.matrix.core.model.events.Event

interface AppserviceEventService {

    enum class EventProcessingState {
        PROCESSED, NOT_PROCESSED
    }

    suspend fun eventProcessingState(tnxId: String, eventId: EventId): EventProcessingState
    suspend fun onEventProcessed(tnxId: String, eventId: EventId)

    suspend fun processEvent(event: Event<*>)
}