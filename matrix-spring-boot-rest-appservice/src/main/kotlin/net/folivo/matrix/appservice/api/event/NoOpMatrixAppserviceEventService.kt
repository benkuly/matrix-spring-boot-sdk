package net.folivo.matrix.appservice.api.event

import net.folivo.matrix.core.model.events.Event

class NoOpMatrixAppserviceEventService : MatrixAppserviceEventService {
    override fun eventProcessingState(
            tnxId: String,
            eventIdOrType: String
    ): MatrixAppserviceEventService.EventProcessingState {
        return MatrixAppserviceEventService.EventProcessingState.PROCESSED
    }

    override fun saveEventProcessed(tnxId: String, eventIdOrType: String) {

    }

    override fun processEvent(event: Event<*>) {

    }
}