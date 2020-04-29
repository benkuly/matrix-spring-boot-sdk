package net.folivo.matrix.appservice.api

import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.restclient.MatrixClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class DefaultAppserviceHandler(
        private val matrixClient: MatrixClient,
        private val matrixUserService: MatrixUserService
) : AppserviceHandler {
    override fun addTransactions(tnxId: String, events: Flux<Event<*>>): Mono<Void> {
        TODO("Not yet implemented")
    }

    override fun hasUser(userId: String): Mono<Void> {
        return if (matrixUserService.hasUser(userId)) {
            Mono.empty()
        } else if (matrixUserService.shouldCreateUser(userId)) {
            TODO("create user with matrix api")
            Mono.empty()
        } else
            Mono.error(MatrixNotFoundException())

    }

    override fun hasRoomAlias(roomAlias: String): Mono<Void> {
        TODO("Not yet implemented")
    }
}