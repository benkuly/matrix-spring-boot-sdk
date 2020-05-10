package net.folivo.matrix.bot.appservice.event

import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService
import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService.EventProcessingState
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.StateEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// FIXME test
class DefaultMatrixAppserviceEventService(
        private val eventTransactionRepository: EventTransactionRepository,
        private val eventHandler: List<MatrixEventHandler>
) : MatrixAppserviceEventService {

    private val logger = LoggerFactory.getLogger(DefaultMatrixAppserviceEventService::class.java)

    override fun eventProcessingState(
            tnxId: String,
            eventIdOrType: String
    ): Mono<EventProcessingState> {
        return eventTransactionRepository.findByTnxIdAndEventIdElseType(tnxId, eventIdOrType)
                .map { EventProcessingState.PROCESSED }
                .switchIfEmpty(Mono.just(EventProcessingState.NOT_PROCESSED))
    }

    override fun saveEventProcessed(tnxId: String, eventIdOrType: String): Mono<Void> {
        return eventTransactionRepository.save(EventTransaction(tnxId, eventIdOrType))
                .then()
    }

    override fun processEvent(event: Event<*>): Mono<Void> {
        return when (event) {
            is MessageEvent<*>  -> delegateEventHandling(event, event.roomId)
            is StateEvent<*, *> -> delegateEventHandling(event, event.roomId)
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