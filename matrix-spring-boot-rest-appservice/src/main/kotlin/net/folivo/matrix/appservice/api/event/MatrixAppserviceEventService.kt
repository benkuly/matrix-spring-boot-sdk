package net.folivo.matrix.appservice.api.event

import net.folivo.matrix.core.model.events.Event

interface MatrixAppserviceEventService {

    enum class EventProcessingState {
        PROCESSED, NOT_PROCESSED
    }

    suspend fun eventProcessingState(tnxId: String, eventIdOrType: String): EventProcessingState
    suspend fun saveEventProcessed(tnxId: String, eventIdOrType: String)

    suspend fun processEvent(event: Event<*>)
}