package net.folivo.matrix.appservice.api

interface MatrixAppserviceEventService {

    enum class EventProcessingState {
        PROCESSED, NOT_PROCESSED
    }

    fun eventProcessingState(tnxId: String, eventIdOrType: String): EventProcessingState
    fun saveEventProcessed(tnxId: String, eventIdOrType: String)
}