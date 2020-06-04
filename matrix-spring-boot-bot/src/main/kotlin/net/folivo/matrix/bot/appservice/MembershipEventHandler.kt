package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode.DISABLED
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode.RESTRICTED
import net.folivo.matrix.bot.config.MatrixBotProperties.TrackMembershipMode
import net.folivo.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.ALL
import net.folivo.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.MANAGED
import net.folivo.matrix.bot.handler.AutoJoinService
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.*
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.FORBIDDEN
import reactor.core.publisher.Mono

class MembershipEventHandler(
        private val autoJoinService: AutoJoinService,
        private val matrixClient: MatrixClient,
        private val roomService: MatrixAppserviceRoomService,
        private val helper: AppserviceHandlerHelper,
        private val asUsername: String,
        private val usersRegex: List<String>,
        private val autoJoin: MatrixBotProperties.AutoJoinMode,
        private val serverName: String,
        private val trackMembershipMode: TrackMembershipMode
) : MatrixEventHandler {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override fun supports(clazz: Class<*>): Boolean {
        return clazz == MemberEvent::class.java
    }

    override fun handleEvent(event: Event<*>, roomId: String?): Mono<Void> {
        if (event is MemberEvent) {
            if (roomId == null) {
                LOG.warn("could not handle join event due to missing roomId")
                return Mono.empty()
            }

            val userId = event.stateKey
            val username = userId.trimStart('@').substringBefore(":")
            val isAsUser = username == asUsername
            val isManagedUser = usersRegex.map { username.matches(Regex(it)) }.contains(true)

            return when (event.content.membership) {
                INVITE     -> {
                    if (isAsUser || isManagedUser) {
                        val asUserId = if (isAsUser) null else userId
                        if (autoJoin == DISABLED) {
                            Mono.empty()
                        } else if (autoJoin == RESTRICTED && roomId.substringAfter(":") != serverName) {
                            LOG.warn("reject room invite of $userId to $roomId because autoJoin is restricted to $serverName")
                            matrixClient.roomsApi.leaveRoom(roomId = roomId, asUserId = asUserId)
                                    .onErrorResume { handleForbidden(it, "leave room") }
                        } else {
                            autoJoinService.shouldJoin(roomId, userId, isAsUser)
                                    .flatMap { shouldJoin ->
                                        if (shouldJoin) {
                                            LOG.debug("join room $roomId with $userId")
                                            matrixClient.roomsApi.joinRoom(
                                                    roomIdOrAlias = roomId,
                                                    asUserId = asUserId
                                            ).onErrorResume { error ->
                                                registerOnMatrixException(userId, error)
                                                        .then(Mono.just(true)) // TODO scary workaround
                                                        .flatMap {
                                                            matrixClient.roomsApi.joinRoom(
                                                                    roomIdOrAlias = roomId,
                                                                    asUserId = asUserId
                                                            )
                                                        }
                                            }.then()
                                        } else {
                                            LOG.debug("reject room invite of $userId to $roomId because autoJoin denied by service")
                                            matrixClient.roomsApi.leaveRoom(
                                                    roomId = roomId,
                                                    asUserId = asUserId
                                            ).onErrorResume { error ->
                                                registerOnMatrixException(userId, error)
                                                        .and(
                                                                matrixClient.roomsApi.leaveRoom(
                                                                        roomId = roomId,
                                                                        asUserId = asUserId
                                                                )
                                                        )
                                            }
                                        }
                                    }
                        }
                    } else {
                        LOG.debug("invited user $userId not managed by this application service.")
                        Mono.empty()
                    }
                }
                JOIN       -> {
                    if (trackMembershipMode == MANAGED && (isAsUser || isManagedUser) || trackMembershipMode == ALL) {
                        LOG.debug("save room join of user $userId and room $roomId")
                        roomService.saveRoomJoin(roomId, userId)
                    } else Mono.empty()
                }
                LEAVE, BAN -> {
                    if (trackMembershipMode == MANAGED && (isAsUser || isManagedUser) || trackMembershipMode == ALL) {
                        LOG.debug("save room leave of user $userId and room $roomId")
                        roomService.saveRoomLeave(roomId, userId)
                    } else Mono.empty()
                }
                else       -> Mono.empty()
            }
        } else return Mono.empty()
    }

    private fun registerOnMatrixException(userId: String, error: Throwable): Mono<Void> {
        return if (error is MatrixServerException && error.statusCode == FORBIDDEN) {
            LOG.warn("try to register user because of ${error.errorResponse}")
            helper.registerAndSaveUser(userId)
                    .onErrorResume { handleForbidden(it, "register user").map { false } }.then()
        } else Mono.error(error)
    }

    private fun handleForbidden(error: Throwable, action: String): Mono<Void> {
        return if (error is MatrixServerException && error.statusCode == FORBIDDEN) {
            LOG.warn("could not $action due to: ${error.errorResponse}")
            Mono.empty()
        } else {
            Mono.error(error)
        }
    }

}