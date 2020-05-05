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
        return events.flatMap { event ->
            val eventIdOrType = when (event) {
                is RoomEvent<*, *>  -> event.id
                is StateEvent<*, *> -> event.id
                else                -> event.type
            }
            logger.debug("incoming event $eventIdOrType in transaction $tnxId")
            matrixAppserviceEventService.eventProcessingState(tnxId, eventIdOrType)
                    .flatMap { eventProcessingState ->
                        when (eventProcessingState) {
                            MatrixAppserviceEventService.EventProcessingState.NOT_PROCESSED -> {
                                logger.debug("process event $eventIdOrType in transaction $tnxId")
                                matrixAppserviceEventService.processEvent(event)
                                        .thenReturn(true)//TODO fix this hacky workaround (without this saveEventProcessed never gets called due to empty mono
                                        .flatMap {
                                            matrixAppserviceEventService.saveEventProcessed(
                                                    tnxId,
                                                    eventIdOrType
                                            )
                                        }
                            }
                            MatrixAppserviceEventService.EventProcessingState.PROCESSED     -> {
                                logger.debug("event $eventIdOrType in transaction $tnxId already processed")
                                Mono.empty()
                            }
                            else                                                            -> {
                                Mono.empty()
                            }
                        }
                    }
        }.then()
    }

    override fun hasUser(userId: String): Mono<Boolean> {
        return matrixAppserviceUserService.userExistingState(userId)
                .flatMap { userExistingState ->
                    when (userExistingState) {
                        UserExistingState.EXISTS          -> Mono.just(true)
                        UserExistingState.DOES_NOT_EXISTS -> Mono.just(false)
                        UserExistingState.CAN_BE_CREATED  -> {
                            logger.debug("started user creation of $userId")
                            matrixClient.userApi
                                    .register(
                                            authenticationType = "m.login.application_service",
                                            username = userId.trimStart('@').substringBefore(":")
                                    )
                                    .flatMap {
                                        matrixAppserviceUserService.saveUser(userId)
                                                .onErrorResume { Mono.empty() }
                                                .doOnError { logger.error("an error occurred in saving user: $it") }
                                    }
                                    .thenReturn(true)//TODO fix this hacky workaround (see above)
                                    .flatMap {
                                        matrixAppserviceUserService.getCreateUserParameter(userId)
                                                .filter { it.displayName != null }
                                                .flatMap { matrixClient.userApi.setDisplayName(userId, it.displayName) }
                                                .onErrorResume { Mono.empty() }
                                                .doOnError { logger.error("an error occurred in setting displayName: $it") }
                                    }
                                    .then(Mono.just(true))
                        }
                        else                              -> {
                            Mono.just(false)
                        }
                    }
                }
    }

    override fun hasRoomAlias(roomAlias: String): Mono<Boolean> {
        return matrixAppserviceRoomService.roomExistingState(roomAlias)
                .flatMap { roomExistingState ->
                    when (roomExistingState) {
                        RoomExistingState.EXISTS          -> Mono.just(true)
                        RoomExistingState.DOES_NOT_EXISTS -> Mono.just(false)
                        RoomExistingState.CAN_BE_CREATED  -> {
                            logger.debug("started room creation of $roomAlias")
                            matrixAppserviceRoomService.getCreateRoomParameter(roomAlias)
                                    .flatMap { createRoomParameter ->
                                        matrixClient.roomsApi
                                                .createRoom(
                                                        roomAliasName = roomAlias.trimStart('#').substringBefore(":"),
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
                        else                              -> {
                            Mono.just(false)
                        }
                    }
                }
    }
}