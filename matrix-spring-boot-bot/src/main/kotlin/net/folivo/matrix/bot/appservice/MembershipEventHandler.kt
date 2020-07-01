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

    override suspend fun handleEvent(event: Event<*>, roomId: String?) {
        if (event is MemberEvent) {
            if (roomId == null) {
                LOG.warn("could not handle join event due to missing roomId")
                return
            }

            val userId = event.stateKey
            val username = userId.trimStart('@').substringBefore(":")
            val isAsUser = username == asUsername
            val isManagedUser = usersRegex.map { username.matches(Regex(it)) }.contains(true)

            when (event.content.membership) {
                INVITE     -> {
                    if (isAsUser || isManagedUser) {
                        val asUserId = if (isAsUser) null else userId
                        if (autoJoin == DISABLED) {
                            return
                        } else if (autoJoin == RESTRICTED && roomId.substringAfter(":") != serverName) {
                            LOG.warn("reject room invite of $userId to $roomId because autoJoin is restricted to $serverName")
                            try {
                                matrixClient.roomsApi.leaveRoom(roomId = roomId, asUserId = asUserId)
                            } catch (error: Throwable) {
                                handleForbidden(error, "leave room")
                            }
                        } else {
                            if (autoJoinService.shouldJoin(roomId, userId, isAsUser)) {
                                LOG.debug("join room $roomId with $userId")
                                try {
                                    matrixClient.roomsApi.joinRoom(roomIdOrAlias = roomId, asUserId = asUserId)
                                } catch (error: Throwable) {
                                    registerOnMatrixException(userId, error)
                                    matrixClient.roomsApi.joinRoom(roomIdOrAlias = roomId, asUserId = asUserId)
                                }
                            } else {
                                LOG.debug("reject room invite of $userId to $roomId because autoJoin denied by service")
                                try {
                                    matrixClient.roomsApi.leaveRoom(roomId = roomId, asUserId = asUserId)
                                } catch (error: Throwable) {
                                    registerOnMatrixException(userId, error)
                                    matrixClient.roomsApi.leaveRoom(roomId = roomId, asUserId = asUserId)
                                }
                            }
                        }
                    } else {
                        LOG.debug("invited user $userId not managed by this application service.")
                    }
                }
                JOIN       -> {
                    if (trackMembershipMode == MANAGED && (isAsUser || isManagedUser) || trackMembershipMode == ALL) {
                        LOG.debug("save room join of user $userId and room $roomId")
                        roomService.saveRoomJoin(roomId, userId)
                    }
                }
                LEAVE, BAN -> {
                    if (trackMembershipMode == MANAGED && (isAsUser || isManagedUser) || trackMembershipMode == ALL) {
                        LOG.debug("save room leave of user $userId and room $roomId")
                        roomService.saveRoomLeave(roomId, userId)
                    }
                }
                else       -> {
                }
            }
        }
    }

    private suspend fun registerOnMatrixException(userId: String, error: Throwable) {
        if (error is MatrixServerException && error.statusCode == FORBIDDEN) {
            LOG.warn("try to register user because of ${error.errorResponse}")
            try {
                helper.registerAndSaveUser(userId)
            } catch (registerError: Throwable) {
                handleForbidden(registerError, "register user")
            }
        } else throw error
    }

    private fun handleForbidden(error: Throwable, action: String) {
        if (error is MatrixServerException && error.statusCode == FORBIDDEN) {
            LOG.warn("could not $action due to: ${error.errorResponse}")
        } else {
            throw error
        }
    }

}