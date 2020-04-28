package net.folivo.matrix.appservice.api

import net.folivo.matrix.core.model.events.Event
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AppserviceHandler {
    fun addTransactions(tnxId: String, events: Flux<Event<*>>): Mono<Void>
    fun hasUser(userId: String): Mono<Void>
    fun hasRoomAlias(roomAlias: String): Mono<Void>
}