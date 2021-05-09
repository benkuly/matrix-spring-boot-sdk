package net.folivo.spring.matrix.bot.membership

import kotlinx.coroutines.flow.Flow
import net.folivo.spring.matrix.bot.room.MatrixRoomService
import net.folivo.spring.matrix.bot.user.MatrixUserService
import net.folivo.trixnity.core.model.MatrixId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MatrixMembershipService(
    private val membershipRepository: MatrixMembershipRepository,
    private val userService: MatrixUserService,
    private val roomService: MatrixRoomService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun getMembership(id: String): MatrixMembership? {
        return membershipRepository.findById(id)
    }

    suspend fun getOrCreateMembership(userId: MatrixId.UserId, roomId: MatrixId.RoomId): MatrixMembership {
        roomService.getOrCreateRoom(roomId)
        userService.getOrCreateUser(userId)
        return membershipRepository.findByUserIdAndRoomId(userId, roomId)
            ?: membershipRepository.save(MatrixMembership(userId, roomId))
    }

    suspend fun getMembershipsByRoomId(roomId: MatrixId.RoomId): Flow<MatrixMembership> {
        return membershipRepository.findByRoomId(roomId)
    }

    suspend fun getMembershipsByUserId(userId: MatrixId.UserId): Flow<MatrixMembership> {
        return membershipRepository.findByUserId(userId)
    }

    suspend fun getMembershipsSizeByUserId(userId: MatrixId.UserId): Long {
        return membershipRepository.countByUserId(userId)
    }

    suspend fun getMembershipsSizeByRoomId(roomId: MatrixId.RoomId): Long {
        return membershipRepository.countByRoomId(roomId)
    }

    suspend fun hasRoomOnlyManagedUsersLeft(roomId: MatrixId.RoomId): Boolean {
        return membershipRepository.containsOnlyManagedMembersByRoomId(roomId)
    }

    suspend fun deleteMembership(userId: MatrixId.UserId, roomId: MatrixId.RoomId) {
        membershipRepository.deleteByUserIdAndRoomId(userId, roomId)
    }

    suspend fun doesRoomContainsMembers(roomId: MatrixId.RoomId, members: Set<MatrixId.UserId>): Boolean {
        return membershipRepository.containsMembersByRoomId(roomId, members)
    }
}