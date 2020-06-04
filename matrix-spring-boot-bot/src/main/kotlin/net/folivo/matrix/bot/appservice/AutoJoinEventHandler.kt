package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.handler.AutoJoinService
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.INVITE
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.FORBIDDEN
import reactor.core.publisher.Mono

class AutoJoinEventHandler(
        private val autoJoinService: AutoJoinService,
        private val matrixClient: MatrixClient,
        private val roomService: MatrixAppserviceRoomService,
        private val helper: AppserviceHandlerHelper,
        private val asUsername: String,
        private val usersRegex: List<String>,
        private val autoJoin: MatrixBotProperties.AutoJoinMode,
        private val serverName: String
) : MatrixEventHandler {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override fun supports(clazz: Class<*>): Boolean {
        return clazz == MemberEvent::class.java
    }

    override fun handleEvent(event: Event<*>, roomId: String?): Mono<Void> {
        if (autoJoin != MatrixBotProperties.AutoJoinMode.DISABLED // TODO should leave room as default action
            && event is MemberEvent
            && event.content.membership == INVITE
        ) {
            if (roomId == null) {
                LOG.warn("could not handle join event due to missing roomId")
                return Mono.empty()
            }

            val invitedUser = event.stateKey
            val invitedUsername = invitedUser.trimStart('@').substringBefore(":")
            val isAsUser = invitedUsername == asUsername
            val asUserId = if (isAsUser) null else invitedUser

            return if (autoJoin == MatrixBotProperties.AutoJoinMode.RESTRICTED
                       && roomId.substringAfter(":") != serverName
            ) {
                LOG.warn("reject room invite of $invitedUser to $roomId because autoJoin is restricted to $serverName")
                matrixClient.roomsApi.leaveRoom(roomId = roomId, asUserId = asUserId)
                        .onErrorResume { Mono.empty() }
            } else if (isAsUser
                       || usersRegex.map { invitedUsername.matches(Regex(it)) }.contains(true)) {
                autoJoinService.shouldJoin(roomId, invitedUser, isAsUser)
                        .flatMap { shouldJoin ->
                            if (shouldJoin) {
                                LOG.debug("join room $roomId with $invitedUser")
                                matrixClient.roomsApi.joinRoom(roomIdOrAlias = roomId, asUserId = asUserId)
                                        .onErrorResume { error ->
                                            registerOnMatrixException(invitedUser, error)
                                                    .then(Mono.just(true)) // TODO scary workaround
                                                    .flatMap {
                                                        matrixClient.roomsApi.joinRoom(
                                                                roomIdOrAlias = roomId,
                                                                asUserId = asUserId
                                                        )
                                                    }
                                        }
                                        .flatMap { roomService.saveRoomJoin(it, invitedUser) }
                            } else {
                                LOG.debug("reject room invite of $invitedUser to $roomId because autoJoin denied by service")
                                matrixClient.roomsApi.leaveRoom(roomId = roomId, asUserId = asUserId)
                                        .onErrorResume { error ->
                                            registerOnMatrixException(invitedUser, error)
                                                    .and(
                                                            matrixClient.roomsApi.leaveRoom(
                                                                    roomId = roomId,
                                                                    asUserId = asUserId
                                                            )
                                                    )
                                        }
                            }
                        }
            } else {
                LOG.debug("invited user $invitedUser to room $roomId not managed by this application service.")
                Mono.empty()
            }
        }
        return Mono.empty()
    }

    private fun registerOnMatrixException(userId: String, error: Throwable): Mono<Void> {
        return if (error is MatrixServerException && error.statusCode == FORBIDDEN) {
            LOG.warn("try to register user because of ${error.errorResponse}")
            helper.registerAndSaveUser(userId).then()
        } else Mono.error(error)
    }
}