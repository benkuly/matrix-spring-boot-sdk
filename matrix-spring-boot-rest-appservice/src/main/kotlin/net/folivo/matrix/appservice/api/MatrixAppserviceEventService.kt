package net.folivo.matrix.appservice.api

import net.folivo.matrix.core.model.events.Event

interface MatrixAppserviceEventService {

    enum class EventProcessingState {
        PROCESSED, NOT_PROCESSED
    }

    fun eventProcessingState(tnxId: String, eventIdOrType: String): EventProcessingState
    fun saveEventProcessed(tnxId: String, eventIdOrType: String)

    fun processEvent(event: Event<*>)
}