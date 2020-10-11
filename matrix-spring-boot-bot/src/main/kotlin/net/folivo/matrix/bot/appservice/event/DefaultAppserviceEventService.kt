package net.folivo.matrix.bot.appservice.event

import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.folivo.matrix.appservice.api.event.AppserviceEventService
import net.folivo.matrix.appservice.api.event.AppserviceEventService.EventProcessingState
import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.StateEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import org.slf4j.LoggerFactory

open class DefaultAppserviceEventService(
        private val eventHandler: List<MatrixEventHandler>,
        private val eventTransactionRepository: MatrixEventTransactionRepository
) : AppserviceEventService {
    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun eventProcessingState(tnxId: String, eventId: String): EventProcessingState {
        return if (eventTransactionRepository.findByTnxIdAndEventIdElseType(tnxId, eventId)
                        .awaitFirstOrNull() != null)
            EventProcessingState.PROCESSED
        else
            EventProcessingState.NOT_PROCESSED
    }

    override suspend fun onEventProcessed(tnxId: String, eventId: String) {
        eventTransactionRepository.save(MatrixEventTransaction(tnxId, eventId)).awaitFirstOrNull()
    }

    override suspend fun processEvent(event: Event<*>) {
        when (event) {
            is MessageEvent<*> -> delegateEventHandling(event, event.roomId)
            is StateEvent<*, *> -> delegateEventHandling(event, event.roomId)
            else                -> delegateEventHandling(event)
        }
    }

    private suspend fun delegateEventHandling(event: Event<*>, roomId: String? = null) {
        LOG.debug("delegate event $event to event handlers")
        eventHandler
                .filter { it.supports(event::class.java) }
                .forEach {
                    it.handleEvent(event, roomId)
                }
    }
}