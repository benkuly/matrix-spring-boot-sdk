package net.folivo.matrix.appservice.api

import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.user.RegisterResponse
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

class AppserviceHandlerHelper(
        private val matrixClient: MatrixClient,
        private val matrixAppserviceUserService: MatrixAppserviceUserService,
        private val matrixAppserviceRoomService: MatrixAppserviceRoomService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    fun registerAndSaveUser(userId: String): Mono<Boolean> {
        return matrixClient.userApi
                .register(
                        authenticationType = "m.login.application_service",
                        username = userId.trimStart('@').substringBefore(":")
                ).onErrorResume {
                    if (it is MatrixServerException && it.errorResponse.errorCode == "M_USER_IN_USE") {
                        LOG.warn("user has already been created")
                        Mono.just(RegisterResponse(userId))
                    } else Mono.error(it)
                }.flatMap {
                    Mono.zipDelayError(
                            matrixAppserviceUserService.saveUser(userId)
                                    .doOnError { LOG.error("an error occurred in saving user: $userId", it) }
                                    .onErrorResume { Mono.empty() },
                            matrixAppserviceUserService.getCreateUserParameter(userId)
                                    .filter { it.displayName != null }
                                    .flatMap {
                                        matrixClient.userApi.setDisplayName(
                                                userId,
                                                it.displayName,
                                                asUserId = userId
                                        )
                                    }
                                    .doOnError { LOG.error("an error occurred in setting displayName", it) }
                                    .onErrorResume { Mono.empty() }
                    )
                }.then(Mono.just(true))
    }

    fun createAndSaveRoom(roomAlias: String): Mono<Boolean> {
        return matrixAppserviceRoomService.getCreateRoomParameter(roomAlias)
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
                }.flatMap { roomId ->
                    matrixAppserviceRoomService.saveRoom(roomAlias, roomId)
                            .doOnError { LOG.error("an error occurred in saving room: $roomId", it) }
                            .onErrorResume { Mono.empty() }
                }
                .then(Mono.just(true))
    }
}