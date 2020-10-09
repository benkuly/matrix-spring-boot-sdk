package net.folivo.matrix.bot.appservice.room

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.folivo.matrix.bot.appservice.membership.MatrixMembershipService
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory

class MatrixRoomService(
        private val roomRepository: MatrixRoomRepository,
        private val matrixClient: MatrixClient,
        private val membershipService: MatrixMembershipService
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun getOrCreateRoom(roomId: String): MatrixRoom { //FIXME test
        val room = roomRepository.findById(roomId).awaitFirstOrNull()
                   ?: roomRepository.save(MatrixRoom(roomId)).awaitFirst()
        val membershipsSize = membershipService.getMembershipsSizeByRoomId(roomId)
        if (membershipsSize == 0L) {// this is needed to get all members, e.g. when managed user joins a new room
            LOG.debug("collect all members in room $roomId because we didn't saved it yet")
            matrixClient.roomsApi.getJoinedMembers(roomId).joined.keys
                    .forEach { joinedUserId ->
                        membershipService.getOrCreateMembership(joinedUserId, roomId)
                    }
        }
        return room
    }

    fun getRoomsByUserId(userId: String): Flow<MatrixRoom> {
        return roomRepository.findByMember(userId).asFlow()
    }

    fun getRoomsByMembers(members: Set<String>): Flow<MatrixRoom> {
        return roomRepository.findByContainingMembers(members).asFlow()
    }

    suspend fun deleteRoom(roomId: String) {
        roomRepository.deleteById(roomId).awaitFirstOrNull()
    }

    suspend fun deleteAllRooms() {
        roomRepository.deleteAll().awaitFirstOrNull()
    }
}