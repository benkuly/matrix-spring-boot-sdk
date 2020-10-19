package net.folivo.matrix.bot.appservice.membership

import kotlinx.coroutines.flow.Flow
import net.folivo.matrix.bot.appservice.room.MatrixRoomService
import net.folivo.matrix.bot.appservice.user.MatrixUserService
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId
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

    suspend fun getOrCreateMembership(userId: UserId, roomId: RoomId): MatrixMembership {
        roomService.getOrCreateRoom(roomId)
        userService.getOrCreateUser(userId)
        return membershipRepository.findByUserIdAndRoomId(userId, roomId)
               ?: membershipRepository.save(MatrixMembership(userId, roomId))
    }

    suspend fun getMembershipsByRoomId(roomId: RoomId): Flow<MatrixMembership> {
        return membershipRepository.findByRoomId(roomId)
    }

    suspend fun getMembershipsByUserId(userId: UserId): Flow<MatrixMembership> {
        return membershipRepository.findByUserId(userId)
    }

    suspend fun getMembershipsSizeByUserId(userId: UserId): Long {
        return membershipRepository.countByUserId(userId)
    }

    suspend fun getMembershipsSizeByRoomId(roomId: RoomId): Long {
        return membershipRepository.countByRoomId(roomId)
    }

    suspend fun hasRoomOnlyManagedUsersLeft(roomId: RoomId): Boolean {
        return membershipRepository.containsOnlyManagedMembersByRoomId(roomId)
    }

    suspend fun deleteMembership(userId: UserId, roomId: RoomId) {
        membershipRepository.deleteByUserIdAndRoomId(userId, roomId)
    }

    suspend fun doesRoomContainsMembers(roomId: RoomId, members: Set<UserId>): Boolean {
        return membershipRepository.containsMembersByRoomId(roomId, members)
    }
}