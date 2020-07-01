package net.folivo.matrix.appservice.api

import kotlinx.coroutines.flow.asFlow
import net.folivo.matrix.appservice.api.event.EventRequest
import net.folivo.matrix.core.api.MatrixServerException
import org.springframework.web.bind.annotation.*

@RestController
class AppserviceController(private val appserviceHandler: AppserviceHandler) {

    /**
     * @see <a href="https://matrix.org/docs/spec/application_service/r0.1.2#put-matrix-app-v1-transactions-txnid">matrix spec</a>
     */
    @PutMapping("/_matrix/app/v1/transactions/{tnxId}", "/transactions/{tnxId}")
    suspend fun addTransactions(@PathVariable tnxId: String, @RequestBody eventRequest: EventRequest): EmptyResponse {
        appserviceHandler.addTransactions(tnxId, eventRequest.events.asFlow())
        return EmptyResponse()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/application_service/r0.1.2#get-matrix-app-v1-users-userid">matrix spec</a>
     */
    @GetMapping("/_matrix/app/v1/users/{userId}", "/users/{userId}")
    suspend fun hasUser(@PathVariable userId: String): EmptyResponse {
        try {
            val hasUser = appserviceHandler.hasUser(userId)
            return if (hasUser) EmptyResponse() else throw MatrixNotFoundException()
        } catch (error: Throwable) {
            if (error !is MatrixServerException) {
                throw MatrixNotFoundException(error.message)
            } else {
                throw error
            }
        }
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/application_service/r0.1.2#get-matrix-app-v1-rooms-roomalias">matrix spec</a>
     */
    @GetMapping("/_matrix/app/v1/rooms/{roomAlias}", "/rooms/{roomAlias}")
    suspend fun hasRoomAlias(@PathVariable roomAlias: String): EmptyResponse {
        try {
            val hasRoomAlias = appserviceHandler.hasRoomAlias(roomAlias)
            return if (hasRoomAlias) EmptyResponse() else throw MatrixNotFoundException()
        } catch (error: Throwable) {
            if (error !is MatrixServerException) {
                throw MatrixNotFoundException(error.message)
            } else {
                throw error
            }
        }
    }
}