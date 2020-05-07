package net.folivo.matrix.bot.appservice.event

import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService
import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService.EventProcessingState
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.StateEvent
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

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
    ): Mono<EventProcessingState> {
        return eventTransactionRepository.existsByTnxIdAndEventIdOrType(tnxId, eventIdOrType)
                .map {
                    if (it) EventProcessingState.PROCESSED else EventProcessingState.NOT_PROCESSED
                }
    }

    override fun saveEventProcessed(tnxId: String, eventIdOrType: String): Mono<Void> {
        return eventTransactionRepository.save(EventTransaction(tnxId, eventIdOrType))
                .then()
    }

    override fun processEvent(event: Event<*>): Mono<Void> {
        return when (event) {
            is MemberEvent      -> {
                if (!allowFederation
                    && event.content.membership == MemberEvent.MemberEventContent.Membership.INVITE
                    && event.sender.substringAfter(":") != serverName
                ) {
                    logger.warn("reject room invite to ${event.roomId} because federation with user ${event.sender} was denied")
                    val roomId = event.roomId
                    if (roomId != null) {
                        matrixClient.roomsApi.leaveRoom(roomId, event.stateKey)
                    } else {
                        Mono.empty()
                    }
                } else {
                    delegateEventHandling(event, event.roomId)
                }
            }
            is MessageEvent<*>  -> {
                if (!allowFederation && event.sender.substringAfter(":") != serverName) {
                    logger.warn("didn't handle message event because federation with user ${event.sender} was denied")
                    Mono.empty<Void>()
                } else {
                    delegateEventHandling(event, event.roomId)
                }
            }
            is StateEvent<*, *> -> {
                if (!allowFederation && event.sender.substringAfter(":") != serverName) {
                    logger.warn("didn't state message event because federation with user ${event.sender} was denied")
                    Mono.empty<Void>()
                } else {
                    delegateEventHandling(event, event.roomId)
                }
            }
            else                -> delegateEventHandling(event)
        }
    }

    private fun delegateEventHandling(event: Event<*>, roomId: String? = null): Mono<Void> {
        logger.debug("delegate event $event to event handlers")
        return Flux.fromIterable(eventHandler)
                .filter { it.supports(event::class.java) }
                .flatMap {
                    it.handleEvent(event, roomId)
                }.then()
    }
}