package net.folivo.matrix.bot.membership

import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode.DISABLED
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode.RESTRICTED
import net.folivo.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.ALL
import net.folivo.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.MANAGED
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.*
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory

class MembershipChangeHandler(//FIXME test
        private val matrixClient: MatrixClient,
        private val membershipChangeService: MembershipChangeService,
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
                        matrixClient.roomsApi.leaveRoom(roomId = roomId, asUserId = asUserId)
                    } else {
                        if (membershipChangeService.shouldJoinRoom(roomId, userId)) {
                            LOG.debug("join room $roomId with $userId")
                            matrixClient.roomsApi.joinRoom(roomIdOrAlias = roomId, asUserId = asUserId)
                        } else {
                            LOG.debug("reject room invite of $userId to $roomId because autoJoin denied by service")
                            matrixClient.roomsApi.leaveRoom(roomId = roomId, asUserId = asUserId)
                        }
                    }
                } else {
                    LOG.debug("invited user $userId not managed.")
                }
            }
            JOIN -> {
                if (trackMembershipMode == MANAGED && (isAsUser || isManagedUser) || trackMembershipMode == ALL) {
                    LOG.debug("save room join of user $userId and room $roomId")
                    membershipChangeService.onRoomJoin(roomId, userId)
                }
            }
            LEAVE, BAN -> {
                if (trackMembershipMode == MANAGED && (isAsUser || isManagedUser) || trackMembershipMode == ALL) {
                    LOG.debug("save room leave of user $userId and room $roomId")
                    membershipChangeService.onRoomLeave(roomId, userId)
                }
            }
            else       -> {
            }
        }
    }
}