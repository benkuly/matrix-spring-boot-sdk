package net.folivo.matrix.bot.appservice.event

import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService
import net.folivo.matrix.core.handler.MatrixEventHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.StateEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent

// FIXME test
class DefaultMatrixAppserviceEventService(
        private val eventTransactionRepository: EventTransactionRepository,
        private val eventHandler: List<MatrixEventHandler>
) : MatrixAppserviceEventService {

    override fun eventProcessingState(
            tnxId: String,
            eventIdOrType: String
    ): MatrixAppserviceEventService.EventProcessingState {
        return if (eventTransactionRepository.findByTnxIdAndEventIdOrType(tnxId, eventIdOrType) == null)
            MatrixAppserviceEventService.EventProcessingState.NOT_PROCESSED
        else
            MatrixAppserviceEventService.EventProcessingState.PROCESSED
    }

    override fun saveEventProcessed(tnxId: String, eventIdOrType: String) {
        eventTransactionRepository.save(
                EventTransaction(
                        tnxId,
                        eventIdOrType
                )
        )
    }

    override fun processEvent(event: Event<*>) {
        eventHandler
                .filter { it.supports(event::class.java) }
                .forEach {
                    when (event) {
                        is MessageEvent<*>  -> it.handleEvent(event, event.roomId)
                        is StateEvent<*, *> -> it.handleEvent(event, event.roomId)
                        else                -> it.handleEvent(event)
                    }
                }
    }
}