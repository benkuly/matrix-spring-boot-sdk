package net.folivo.matrix.bot.appservice.event

import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.StateEvent
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory

// FIXME test
class DefaultMatrixAppserviceEventService(
        private val matrixClient: MatrixClient,
        private val eventTransactionRepository: EventTransactionRepository,
        private val eventHandler: List<MatrixEventHandler>,
        private val allowFederation: Boolean,
        private val serverName: String
) : MatrixAppserviceEventService {

    private val logger = LoggerFactory.getLogger(DefaultMatrixAppserviceEventService::class.java)

    override fun eventProcessingState(
            tnxId: String,
            eventIdOrType: String
    ): MatrixAppserviceEventService.EventProcessingState {
        return if (eventTransactionRepository.findByTnxIdAndEventIdType(tnxId, eventIdOrType) == null)
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
        when (event) {
            is MemberEvent      -> {
                if (!allowFederation && event.content.membership == MemberEvent.MemberEventContent.Membership.INVITE) {
                    logger.warn("reject room invite to ${event.roomId} because federation with user ${event.sender} was denied")
                    event.roomId?.let { matrixClient.roomsApi.leaveRoom(it, event.stateKey).block() }
                    return
                }
            }
            is MessageEvent<*>  -> {
                if (!allowFederation && event.sender.substringAfter(":") != serverName) {
                    logger.warn("didn't handle message event because federation with user ${event.sender} was denied")
                    return
                }
                handleEvent(event, event.roomId)
            }
            is StateEvent<*, *> -> {
                if (!allowFederation && event.sender.substringAfter(":") != serverName) {
                    logger.warn("didn't state message event because federation with user ${event.sender} was denied")
                    return
                }
                handleEvent(event, event.roomId)
            }
            else                -> handleEvent(event)
        }
    }

    private fun handleEvent(event: Event<*>, roomId: String? = null) {
        eventHandler
                .filter { it.supports(event::class.java) }
                .forEach {
                    it.handleEvent(event, roomId)
                }
    }
}