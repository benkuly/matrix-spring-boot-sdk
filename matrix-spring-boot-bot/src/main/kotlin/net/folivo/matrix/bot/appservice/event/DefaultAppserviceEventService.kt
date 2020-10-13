package net.folivo.matrix.bot.appservice.event

import net.folivo.matrix.appservice.api.event.AppserviceEventService
import net.folivo.matrix.appservice.api.event.AppserviceEventService.EventProcessingState
import net.folivo.matrix.bot.appservice.sync.MatrixSyncService
import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.StateEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import org.slf4j.LoggerFactory

open class DefaultAppserviceEventService(
        private val eventTransactionService: MatrixEventTransactionService,
        private val syncService: MatrixSyncService,
        private val eventHandler: List<MatrixEventHandler>
) : AppserviceEventService {
    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun eventProcessingState(tnxId: String, eventIdOrHash: String): EventProcessingState {
        return if (eventTransactionService.hasEvent(tnxId, eventIdOrHash))
            EventProcessingState.PROCESSED
        else
            EventProcessingState.NOT_PROCESSED
    }

    override suspend fun onEventProcessed(tnxId: String, eventIdOrHash: String) {
        eventTransactionService.saveEvent(MatrixEventTransaction(tnxId, eventIdOrHash))
    }

    override suspend fun processEvent(event: Event<*>) {
        when (event) {
            is MessageEvent<*> -> delegateEventHandling(event, event.roomId)
            is StateEvent<*, *> -> delegateEventHandling(event, event.roomId)
            else                -> delegateEventHandling(event)
        }
    }

    private suspend fun delegateEventHandling(event: Event<*>, roomId: String? = null) {
        if (roomId != null) syncService.syncRoomMemberships(roomId) // FIXME test
        LOG.debug("delegate event $event to event handlers")
        eventHandler
                .filter { it.supports(event::class.java) }
                .forEach {
                    it.handleEvent(event, roomId)
                }
    }
}