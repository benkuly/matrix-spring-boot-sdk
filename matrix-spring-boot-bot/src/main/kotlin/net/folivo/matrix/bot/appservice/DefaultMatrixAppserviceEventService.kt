package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService
import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService.EventProcessingState
import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService.EventProcessingState.NOT_PROCESSED
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.StateEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

open class DefaultMatrixAppserviceEventService(
        private val eventHandler: List<MatrixEventHandler>
) : MatrixAppserviceEventService {
    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override fun eventProcessingState(tnxId: String, eventIdOrType: String): Mono<EventProcessingState> {
        return Mono.just(NOT_PROCESSED)
    }

    override fun saveEventProcessed(tnxId: String, eventIdOrType: String): Mono<Void> {
        return Mono.empty()
    }

    override fun processEvent(event: Event<*>): Mono<Void> {
        return when (event) {
            is MessageEvent<*>  -> delegateEventHandling(event, event.roomId)
            is StateEvent<*, *> -> delegateEventHandling(event, event.roomId)
            else                -> delegateEventHandling(event)
        }
    }

    private fun delegateEventHandling(event: Event<*>, roomId: String? = null): Mono<Void> {
        LOG.debug("delegate event $event to event handlers")
        return Flux.fromIterable(eventHandler)
                .filter { it.supports(event::class.java) }
                .flatMap {
                    it.handleEvent(event, roomId)
                }.then()
    }
}