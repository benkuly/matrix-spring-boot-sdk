package net.folivo.matrix.appservice.api

import net.folivo.matrix.core.model.events.Event
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class DefaultAppserviceHandler : AppserviceHandler {
    override fun addTransactions(tnxId: String, events: Flux<Event<*>>): Mono<Void> {
        TODO("Not yet implemented")
    }

    override fun hasUser(userId: String): Mono<Void> {
        TODO("Not yet implemented")
    }

    override fun hasRoomAlias(roomAlias: String): Mono<Void> {
        TODO("Not yet implemented")
    }
}