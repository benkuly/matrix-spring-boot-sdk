package net.folivo.matrix.appservice.api

import net.folivo.matrix.appservice.api.event.EventRequest
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class AppserviceController(private val appserviceHandler: AppserviceHandler) {

    /**
     * @see <a href="https://matrix.org/docs/spec/application_service/r0.1.2#put-matrix-app-v1-transactions-txnid">matrix spec</a>
     */
    @PutMapping("/_matrix/app/v1/transactions/{tnxId}", "/transactions/{tnxId}")
    fun addTransactions(@PathVariable tnxId: String, @RequestBody eventRequest: EventRequest): Mono<EmptyResponse> {
        return appserviceHandler.addTransactions(tnxId, Flux.fromIterable(eventRequest.events))
                .then(Mono.just(EmptyResponse()))
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/application_service/r0.1.2#get-matrix-app-v1-users-userid">matrix spec</a>
     */
    @GetMapping("/_matrix/app/v1/users/{userId}", "/users/{userId}")
    fun hasUser(@PathVariable userId: String): Mono<EmptyResponse> {
        return appserviceHandler.hasUser(userId)
                .flatMap { if (!it) Mono.error(MatrixNotFoundException()) else Mono.just(EmptyResponse()) }
                .onErrorMap { MatrixNotFoundException(it.message) }
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/application_service/r0.1.2#get-matrix-app-v1-rooms-roomalias">matrix spec</a>
     */
    @GetMapping("/_matrix/app/v1/rooms/{roomAlias}", "/rooms/{roomAlias}")
    fun hasRoomAlias(@PathVariable roomAlias: String): Mono<EmptyResponse> {
        return appserviceHandler.hasRoomAlias(roomAlias)
                .flatMap { if (!it) Mono.error(MatrixNotFoundException()) else Mono.just(EmptyResponse()) }
                .onErrorMap { MatrixNotFoundException(it.message) }
    }
}