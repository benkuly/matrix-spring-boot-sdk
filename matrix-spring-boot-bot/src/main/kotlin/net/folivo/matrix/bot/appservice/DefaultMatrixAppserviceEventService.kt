package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService
import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService.EventProcessingState
import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService.EventProcessingState.NOT_PROCESSED
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.StateEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import org.slf4j.LoggerFactory

open class DefaultMatrixAppserviceEventService(
        private val eventHandler: List<MatrixEventHandler>
) : MatrixAppserviceEventService {
    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun eventProcessingState(tnxId: String, eventIdOrType: String): EventProcessingState {
        return NOT_PROCESSED
    }

    override suspend fun saveEventProcessed(tnxId: String, eventIdOrType: String) {
    }

    override suspend fun processEvent(event: Event<*>) {
        when (event) {
            is MessageEvent<*>  -> delegateEventHandling(event, event.roomId)
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