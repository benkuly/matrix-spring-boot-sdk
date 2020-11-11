package net.folivo.matrix.bot.membership

import kotlinx.coroutines.flow.collect
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.*
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory

class MatrixMembershipSyncService(
        private val matrixClient: MatrixClient,
        private val membershipService: MatrixMembershipService,
        private val helper: BotServiceHelper,
        private val botProperties: MatrixBotProperties
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun syncBotMemberships() {
        try {
            matrixClient.roomsApi.getJoinedRooms()
                    .collect { roomId ->
                        LOG.debug("sync room $roomId")
                        matrixClient.roomsApi.getJoinedMembers(roomId).joined.keys.forEach { userId ->
                            membershipService.getOrCreateMembership(userId, roomId)
                        }
                    }
            LOG.debug("synced bot user members")
        } catch (error: Throwable) {
            LOG.error("tried to sync bot user rooms, but that was not possible: ${error.message}")
        }
    }

    suspend fun syncRoomMemberships(roomId: RoomId) {
        // this is needed to get all members, e.g. when managed user joins a new room
        val trackMembershipMode = botProperties.trackMembership
        if (trackMembershipMode != NONE && membershipService.getMembershipsSizeByRoomId(roomId) == 1L) { // FIXME test
            LOG.debug("collect all members in room $roomId because we didn't saved it yet")
            try {
                matrixClient.roomsApi.getJoinedMembers(roomId).joined.keys
                        .forEach { joinedUserId ->
                            if (trackMembershipMode == ALL
                                || trackMembershipMode == MANAGED && helper.isManagedUser(joinedUserId))
                                membershipService.getOrCreateMembership(joinedUserId, roomId)
                        }
            } catch (error: Throwable) {
                LOG.error("tried to sync room $roomId, but that was not possible: ${error.message}")
            }
        }
    }
}