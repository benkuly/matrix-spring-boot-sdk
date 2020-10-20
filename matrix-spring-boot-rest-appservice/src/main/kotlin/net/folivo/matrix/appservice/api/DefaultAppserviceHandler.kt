package net.folivo.matrix.appservice.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import net.folivo.matrix.appservice.api.event.AppserviceEventService
import net.folivo.matrix.appservice.api.room.AppserviceRoomService
import net.folivo.matrix.appservice.api.room.AppserviceRoomService.RoomExistingState
import net.folivo.matrix.appservice.api.user.AppserviceUserService
import net.folivo.matrix.appservice.api.user.AppserviceUserService.UserExistingState
import net.folivo.matrix.core.model.MatrixId.RoomAliasId
import net.folivo.matrix.core.model.MatrixId.UserId
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.RoomEvent
import net.folivo.matrix.core.model.events.StateEvent
import org.slf4j.LoggerFactory

class DefaultAppserviceHandler(
        private val appserviceEventService: AppserviceEventService,
        private val appserviceUserService: AppserviceUserService,
        private val appserviceRoomService: AppserviceRoomService,
        private val helper: AppserviceHandlerHelper
) : AppserviceHandler {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun addTransactions(tnxId: String, events: Flow<Event<*>>) {
        try {
            events.collect { event ->
                val eventId = when (event) {
                    is RoomEvent<*, *> -> event.id
                    is StateEvent<*, *> -> event.id
                    else                -> null
                }
                LOG.debug("incoming event $eventId in transaction $tnxId")
                if (eventId == null) {
                    LOG.debug("process event $eventId in transaction $tnxId")
                    appserviceEventService.processEvent(event)
                } else when (appserviceEventService.eventProcessingState(tnxId, eventId)) {
                    AppserviceEventService.EventProcessingState.NOT_PROCESSED -> {
                        LOG.debug("process event $eventId in transaction $tnxId")
                        appserviceEventService.processEvent(event)
                        appserviceEventService.onEventProcessed(
                                tnxId,
                                eventId
                        )
                    }
                    AppserviceEventService.EventProcessingState.PROCESSED -> {
                        LOG.debug("event $eventId in transaction $tnxId already processed")
                    }
                }
            }
        } catch (error: Throwable) {
            LOG.error("something went wrong while processing events", error)
            throw error
        }
    }

    override suspend fun hasUser(userId: UserId): Boolean {
        return when (appserviceUserService.userExistingState(userId)) {
            UserExistingState.EXISTS -> true
            UserExistingState.DOES_NOT_EXISTS -> false
            UserExistingState.CAN_BE_CREATED -> {
                LOG.debug("started user creation of $userId")
                helper.registerManagedUser(userId)
                true
            }
        }
    }

    override suspend fun hasRoomAlias(roomAlias: RoomAliasId): Boolean {
        return when (appserviceRoomService.roomExistingState(roomAlias)) {
            RoomExistingState.EXISTS -> true
            RoomExistingState.DOES_NOT_EXISTS -> false
            RoomExistingState.CAN_BE_CREATED -> {
                LOG.debug("started room creation of $roomAlias")
                helper.createManagedRoom(roomAlias)
                true
            }
        }
    }
}