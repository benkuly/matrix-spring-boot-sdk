package net.folivo.spring.matrix.bot.membership

import net.folivo.spring.matrix.bot.config.MatrixBotProperties
import net.folivo.spring.matrix.bot.config.MatrixBotProperties.AutoJoinMode.DISABLED
import net.folivo.spring.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.ALL
import net.folivo.spring.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.MANAGED
import net.folivo.spring.matrix.bot.util.BotServiceHelper
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.core.model.MatrixId
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent.Membership.*
import org.slf4j.LoggerFactory

class MembershipChangeHandler(
    private val matrixClient: MatrixClient,
    private val membershipChangeService: MembershipChangeService,
    private val botHelper: BotServiceHelper,
    private val botProperties: MatrixBotProperties
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun handleMembership(
        userId: MatrixId.UserId,
        roomId: MatrixId.RoomId,
        membership: MemberEventContent.Membership
    ) {
        val isManagedUser = botHelper.isManagedUser(userId)
        val serverName = botProperties.serverName

        when (membership) {
            INVITE -> {
                val autoJoin = botProperties.autoJoin
                if (isManagedUser && autoJoin != DISABLED) {
                    val asUserId = if (userId == botProperties.botUserId) null else userId
                    if (autoJoin == MatrixBotProperties.AutoJoinMode.RESTRICTED && roomId.domain != serverName) {
                        LOG.warn("reject room invite of $userId to $roomId because autoJoin is restricted to $serverName")
                        matrixClient.room.leaveRoom(roomId = roomId, asUserId = asUserId)
                    } else {
                        if (membershipChangeService.shouldJoinRoom(userId, roomId)) {
                            LOG.debug("join room $roomId with $userId")
                            matrixClient.room.joinRoom(roomId = roomId, asUserId = asUserId)
                        } else {
                            LOG.debug("reject room invite of $userId to $roomId because autoJoin denied by service")
                            matrixClient.room.leaveRoom(roomId = roomId, asUserId = asUserId)
                        }
                    }
                } else {
                    LOG.debug("invited user $userId not managed or autoJoin disabled.")
                }
            }
            JOIN -> {
                val trackMembershipMode = botProperties.trackMembership
                if (trackMembershipMode == MANAGED && isManagedUser || trackMembershipMode == ALL) {
                    LOG.debug("save room join of user $userId and room $roomId")
                    membershipChangeService.onRoomJoin(userId, roomId)
                }
            }
            LEAVE, BAN -> {
                val trackMembershipMode = botProperties.trackMembership
                if (trackMembershipMode == MANAGED && isManagedUser || trackMembershipMode == ALL) {
                    LOG.debug("save room leave of user $userId and room $roomId")
                    membershipChangeService.onRoomLeave(userId, roomId)
                }
            }
            else -> {
            }
        }
    }
}