package net.folivo.matrix.appservice.api

import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService.RoomExistingState
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService.UserExistingState
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.RoomEvent
import net.folivo.matrix.core.model.events.StateEvent
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class DefaultAppserviceHandler(
        private val matrixClient: MatrixClient,
        private val matrixAppserviceEventService: MatrixAppserviceEventService,
        private val matrixAppserviceUserService: MatrixAppserviceUserService,
        private val matrixAppserviceRoomService: MatrixAppserviceRoomService
) : AppserviceHandler {

    private val logger = LoggerFactory.getLogger(DefaultAppserviceHandler::class.java)

    override fun addTransactions(tnxId: String, events: Flux<Event<*>>): Mono<Void> {
        return events.map { event ->
            val eventIdOrType = when (event) {
                is RoomEvent<*, *>  -> event.id
                is StateEvent<*, *> -> event.id
                else                -> event.type
            }
            when (matrixAppserviceEventService.eventProcessingState(tnxId, eventIdOrType)) {
                MatrixAppserviceEventService.EventProcessingState.NOT_PROCESSED -> {
                    matrixAppserviceEventService.processEvent(event)
                    matrixAppserviceEventService.saveEventProcessed(tnxId, eventIdOrType)
                }
                MatrixAppserviceEventService.EventProcessingState.PROCESSED     -> {
                    logger.info("event $eventIdOrType already processed")
                }
            }
        }.then()
    }

    override fun hasUser(userId: String): Mono<Boolean> {
        return when (matrixAppserviceUserService.userExistingState(userId)) {
            UserExistingState.EXISTS          -> Mono.just(true)
            UserExistingState.DOES_NOT_EXISTS -> Mono.just(false)
            UserExistingState.CAN_BE_CREATED  -> {
                val username = userId.trimStart('@').substringBefore(":")
                matrixClient.userApi
                        .register(authenticationType = "m.login.application_service", username = username)
                        .doOnSuccess { response ->
                            try {
                                matrixAppserviceUserService.saveUser(response.userId)
                                val createUserParameter = matrixAppserviceUserService.getCreateUserParameter(userId)
                                matrixClient.userApi.setDisplayName(response.userId, createUserParameter.displayName)
                                        .block()
                            } catch (error: Throwable) {
                                logger.error("an error occurred in after user registration tasks: $error")
                            }
                        }
                        .map { true }
            }
        }
    }

    override fun hasRoomAlias(roomAlias: String): Mono<Boolean> {
        return when (matrixAppserviceRoomService.roomExistingState(roomAlias)) {
            RoomExistingState.EXISTS          -> Mono.just(true)
            RoomExistingState.DOES_NOT_EXISTS -> Mono.just(false)
            RoomExistingState.CAN_BE_CREATED  -> {
                val roomAliasName = roomAlias.trimStart('#').substringBefore(":")
                val createRoomParameter = matrixAppserviceRoomService.getCreateRoomParameter(roomAlias)
                matrixClient.roomsApi
                        .createRoom(
                                roomAliasName = roomAliasName,
                                visibility = createRoomParameter.visibility,
                                name = createRoomParameter.name,
                                topic = createRoomParameter.topic,
                                invite = createRoomParameter.invite,
                                invite3Pid = createRoomParameter.invite3Pid,
                                roomVersion = createRoomParameter.roomVersion,
                                asUserId = createRoomParameter.asUserId,
                                creationContent = createRoomParameter.creationContent,
                                initialState = createRoomParameter.initialState,
                                isDirect = createRoomParameter.isDirect,
                                powerLevelContentOverride = createRoomParameter.powerLevelContentOverride,
                                preset = createRoomParameter.preset
                        )
                        .doOnSuccess {
                            try {
                                matrixAppserviceRoomService.saveRoom(roomAlias, it)
                            } catch (error: Throwable) {
                                logger.error("an error occurred in after room creation tasks: $error")
                            }
                        }
                        .map { true }
            }
        }
    }
}