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
import reactor.core.scheduler.Schedulers

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
        return Mono.fromCallable {
            eventTransactionRepository.findByTnxIdAndEventIdType(tnxId, eventIdOrType)
        }.subscribeOn(Schedulers.boundedElastic())
                .map {
                    if (it == null) EventProcessingState.NOT_PROCESSED else EventProcessingState.PROCESSED
                }
    }

    override fun saveEventProcessed(tnxId: String, eventIdOrType: String): Mono<Void> {
        return Mono.fromCallable {
            eventTransactionRepository.save(
                    EventTransaction(
                            tnxId,
                            eventIdOrType
                    )
            )
        }.subscribeOn(Schedulers.boundedElastic())
                .then()
    }

    override fun processEvent(event: Event<*>): Mono<Void> {
        return when (event) {
            is MemberEvent      -> {
                if (!allowFederation && event.content.membership == MemberEvent.MemberEventContent.Membership.INVITE) {
                    logger.warn("reject room invite to ${event.roomId} because federation with user ${event.sender} was denied")
                    event.roomId?.let { matrixClient.roomsApi.leaveRoom(it, event.stateKey) } ?: Mono.empty()
                }
                handleEvent(event, event.roomId)
            }
            is MessageEvent<*>  -> {
                if (!allowFederation && event.sender.substringAfter(":") != serverName) {
                    logger.warn("didn't handle message event because federation with user ${event.sender} was denied")
                    Mono.empty<Void>()
                }
                handleEvent(event, event.roomId)
            }
            is StateEvent<*, *> -> {
                if (!allowFederation && event.sender.substringAfter(":") != serverName) {
                    logger.warn("didn't state message event because federation with user ${event.sender} was denied")
                    Mono.empty<Void>()
                }
                handleEvent(event, event.roomId)
            }
            else                -> handleEvent(event)
        }
    }

    private fun handleEvent(event: Event<*>, roomId: String? = null): Mono<Void> {
        return Flux.fromIterable(eventHandler)
                .filter { it.supports(event::class.java) }
                .flatMap {
                    it.handleEvent(event, roomId)
                }.then()
    }
}