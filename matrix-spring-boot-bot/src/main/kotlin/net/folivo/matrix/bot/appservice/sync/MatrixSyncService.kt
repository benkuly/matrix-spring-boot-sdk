package net.folivo.matrix.bot.appservice.sync

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import net.folivo.matrix.bot.appservice.room.MatrixRoomService
import net.folivo.matrix.bot.membership.MembershipChangeService
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory

class MatrixSyncService(
        private val roomService: MatrixRoomService,
        private val helper: BotServiceHelper,
        private val matrixClient: MatrixClient,
        private val membershipChangeService: MembershipChangeService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun syncMemberships(userId: String? = null) { //FIXME test
        val dbUserId = userId ?: helper.getBotUserId()
        // FIXME maybe delete all memberships of this user?
        if (roomService.getRoomsByUserId(dbUserId).take(1).toList().isEmpty()) {
            try {
                matrixClient.roomsApi.getJoinedRooms(asUserId = userId)
                        .collect { room ->
                            matrixClient.roomsApi.getJoinedMembers(
                                    room,
                                    asUserId = userId
                            ).joined.keys.forEach { user ->
                                membershipChangeService.onRoomJoin(room, user)
                            }
                        }
                LOG.debug("synced user because we didn't know any rooms with him")
            } catch (error: Throwable) {
                LOG.debug("tried to sync user without rooms, but that was not possible: ${error.message}")
            }
        }
    }
}