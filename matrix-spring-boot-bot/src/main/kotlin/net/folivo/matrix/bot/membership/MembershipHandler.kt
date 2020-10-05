package net.folivo.matrix.bot.membership

import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode.DISABLED
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode.RESTRICTED
import net.folivo.matrix.bot.config.MatrixBotProperties.BotMode.APPSERVICE
import net.folivo.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.ALL
import net.folivo.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.MANAGED
import net.folivo.matrix.bot.handler.BotServiceHelper
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.*
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.FORBIDDEN

class MembershipHandler(//FIXME test
        private val autoJoinCustomizer: AutoJoinCustomizer,
        private val matrixClient: MatrixClient,
        private val membershipService: MembershipService,
        private val appserviceHelper: AppserviceHandlerHelper,
        private val botHelper: BotServiceHelper,
        private val botProperties: MatrixBotProperties
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun handleMembership(roomId: String, userId: String, membership: Membership) {
        val isAsUser = userId.trimStart('@').substringBefore(":") == botProperties.username
        val isManagedUser = botHelper.isManagedUser(userId)

        val (autoJoin, trackMembershipMode, serverName) = botProperties

        when (membership) {
            INVITE -> {
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
                            matrixClient.roomsApi.leaveRoom(roomId = roomId, asUserId = asUserId)
                        }
                    } else {
                        if (autoJoinCustomizer.shouldJoin(roomId, userId)) {
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
                    LOG.debug("invited user $userId not managed.")
                }
            }
            JOIN -> {
                if (trackMembershipMode == MANAGED && (isAsUser || isManagedUser) || trackMembershipMode == ALL) {
                    LOG.debug("save room join of user $userId and room $roomId")
                    membershipService.onRoomJoin(roomId, userId)
                }
            }
            LEAVE, BAN -> {
                if (trackMembershipMode == MANAGED && (isAsUser || isManagedUser) || trackMembershipMode == ALL) {
                    LOG.debug("save room leave of user $userId and room $roomId")
                    membershipService.onRoomLeave(roomId, userId)
                }
            }
            else       -> {
            }
        }
    }

    private suspend fun registerOnMatrixException(userId: String, error: Throwable) {
        if (botProperties.mode == APPSERVICE && error is MatrixServerException && error.statusCode == FORBIDDEN) {
            LOG.warn("try to register user because of ${error.errorResponse}")
            try {
                appserviceHelper.registerManagedUser(userId)
            } catch (registerError: Throwable) {
                handleForbidden(registerError, "register user")
            }
        } else throw error
    }

    private fun handleForbidden(error: Throwable, failedAction: String) {
        if (error is MatrixServerException && error.statusCode == FORBIDDEN) {
            LOG.warn("could not $failedAction due to: ${error.errorResponse}")
        } else {
            throw error
        }
    }

}