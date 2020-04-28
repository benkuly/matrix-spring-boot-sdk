package net.folivo.matrix.appservice.api

import net.folivo.matrix.core.model.events.Event
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController("/_matrix/app/v1")
class AppserviceController(private val appserviceHandler: AppserviceHandler) {

    /**
     * @see <a href="https://matrix.org/docs/spec/application_service/r0.1.2#put-matrix-app-v1-transactions-txnid">matrix spec</a>
     */
    @PutMapping("/transactions/{tnxId}")
    fun addTransactions(@PathVariable tnxId: String, @RequestBody events: Flux<Event<*>>): Mono<Void> {
        return appserviceHandler.addTransactions(tnxId, events)
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/application_service/r0.1.2#get-matrix-app-v1-users-userid">matrix spec</a>
     */
    @GetMapping("/users/{userId}")
    fun hasUser(@PathVariable userId: String): Mono<Void> {
        return appserviceHandler.hasUser(userId)
                .onErrorMap { MatrixNotFoundException() } // TODO should we allow Internal Server Error or something similar
    }
    //if user not found 404 with NET.FOLIVO.MATRIX_NOT_FOUND
    //else 200
    //The application service indicates that this user exists.
    // The application service MUST create the user using the client-server API.


    /**
     * @see <a href="https://matrix.org/docs/spec/application_service/r0.1.2#get-matrix-app-v1-rooms-roomalias">matrix spec</a>
     */
    @GetMapping("/rooms/{roomAlias}")
    fun hasRoomAlias(@PathVariable roomAlias: String): Mono<Void> {
        return appserviceHandler.hasRoomAlias(roomAlias)
                .onErrorMap { MatrixNotFoundException() } // TODO should we allow Internal Server Error or something similar
        //if user not found 404 with NET.FOLIVO.MATRIX_NOT_FOUND
        //else 200
        //The application service indicates that this room alias exists.
        // The application service MUST have created a room and associated it with the queried room alias using the client-server API.
        // Additional information about the room such as its name and topic can be set before responding.
    }
}