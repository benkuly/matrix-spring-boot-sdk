package net.folivo.matrix.bot.appservice.sync

import kotlinx.coroutines.flow.collect
import net.folivo.matrix.bot.appservice.membership.MatrixMembershipService
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory

class MatrixSyncService(
        private val matrixClient: MatrixClient,
        private val membershipService: MatrixMembershipService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun syncBotMemberships() {
        try {
            matrixClient.roomsApi.getJoinedRooms()
                    .collect { roomId ->
                        matrixClient.roomsApi.getJoinedMembers(roomId).joined.keys.forEach { userId ->
                            membershipService.getOrCreateMembership(userId, roomId)
                        }
                    }
            LOG.debug("synced bot user members")
        } catch (error: Throwable) {
            LOG.error("tried to sync bot user rooms, but that was not possible: ${error.message}")
        }
    }

    suspend fun syncRoomMemberships(roomId: RoomId) { // FIXME this is user specific. we need to find a way to use the right userId
        if (membershipService.getMembershipsSizeByRoomId(roomId) == 0L) {// this is needed to get all members, e.g. when managed user joins a new room
            LOG.debug("collect all members in room $roomId because we didn't saved it yet")
            try {
                matrixClient.roomsApi.getJoinedMembers(roomId).joined.keys
                        .forEach { joinedUserId ->
                            membershipService.getOrCreateMembership(joinedUserId, roomId)
                        }
            } catch (error: Throwable) {
                LOG.error("tried to sync room $roomId, but that was not possible: ${error.message}")
            }
        }
    }
}