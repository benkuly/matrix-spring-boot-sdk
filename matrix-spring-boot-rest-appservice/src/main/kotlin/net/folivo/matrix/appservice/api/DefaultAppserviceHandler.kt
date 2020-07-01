package net.folivo.matrix.appservice.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService.RoomExistingState
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService.UserExistingState
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.RoomEvent
import net.folivo.matrix.core.model.events.StateEvent
import org.slf4j.LoggerFactory

class DefaultAppserviceHandler(
        private val matrixAppserviceEventService: MatrixAppserviceEventService,
        private val matrixAppserviceUserService: MatrixAppserviceUserService,
        private val matrixAppserviceRoomService: MatrixAppserviceRoomService,
        private val helper: AppserviceHandlerHelper
) : AppserviceHandler {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun addTransactions(tnxId: String, events: Flow<Event<*>>) {
        try {
            events.collect { event ->
                val eventIdOrType = when (event) {
                    is RoomEvent<*, *>  -> event.id
                    is StateEvent<*, *> -> event.id
                    else                -> event.type
                }
                LOG.debug("incoming event $eventIdOrType in transaction $tnxId")
                when (matrixAppserviceEventService.eventProcessingState(tnxId, eventIdOrType)) {
                    MatrixAppserviceEventService.EventProcessingState.NOT_PROCESSED -> {
                        LOG.debug("process event $eventIdOrType in transaction $tnxId")
                        matrixAppserviceEventService.processEvent(event)
                        matrixAppserviceEventService.saveEventProcessed(
                                tnxId,
                                eventIdOrType
                        )
                    }
                    MatrixAppserviceEventService.EventProcessingState.PROCESSED     -> {
                        LOG.debug("event $eventIdOrType in transaction $tnxId already processed")
                    }
                }
            }
        } catch (error: Throwable) {
            LOG.error("something went wrong while processing events", error)
            throw error
        }
    }

    override suspend fun hasUser(userId: String): Boolean {
        return when (matrixAppserviceUserService.userExistingState(userId)) {
            UserExistingState.EXISTS          -> true
            UserExistingState.DOES_NOT_EXISTS -> false
            UserExistingState.CAN_BE_CREATED  -> {
                LOG.debug("started user creation of $userId")
                helper.registerAndSaveUser(userId)
                true
            }
        }
    }

    override suspend fun hasRoomAlias(roomAlias: String): Boolean {
        return when (matrixAppserviceRoomService.roomExistingState(roomAlias)) {
            RoomExistingState.EXISTS          -> true
            RoomExistingState.DOES_NOT_EXISTS -> false
            RoomExistingState.CAN_BE_CREATED  -> {
                LOG.debug("started room creation of $roomAlias")
                helper.createAndSaveRoom(roomAlias)
                true
            }
        }
    }
}