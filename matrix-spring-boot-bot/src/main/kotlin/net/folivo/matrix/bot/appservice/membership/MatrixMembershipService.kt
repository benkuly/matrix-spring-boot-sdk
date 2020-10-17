package net.folivo.matrix.bot.appservice.membership

import kotlinx.coroutines.flow.Flow
import net.folivo.matrix.bot.appservice.room.MatrixRoomService
import net.folivo.matrix.bot.appservice.user.MatrixUserService
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

    suspend fun getOrCreateMembership(userId: String, roomId: String): MatrixMembership {
        roomService.getOrCreateRoom(roomId)
        userService.getOrCreateUser(userId)
        return membershipRepository.findByUserIdAndRoomId(userId, roomId)
               ?: membershipRepository.save(MatrixMembership(userId, roomId))
    }

    suspend fun getMembershipsByRoomId(roomId: String): Flow<MatrixMembership> {
        return membershipRepository.findByRoomId(roomId)
    }

    suspend fun getMembershipsByUserId(userId: String): Flow<MatrixMembership> {
        return membershipRepository.findByUserId(userId)
    }

    suspend fun getMembershipsSizeByUserId(userId: String): Long {
        return membershipRepository.countByUserId(userId)
    }

    suspend fun getMembershipsSizeByRoomId(roomId: String): Long {
        return membershipRepository.countByRoomId(roomId)
    }

    suspend fun hasRoomOnlyManagedUsersLeft(roomId: String): Boolean {
        return membershipRepository.containsOnlyManagedMembersByRoomId(roomId)
    }

    suspend fun deleteMembership(userId: String, roomId: String) {
        membershipRepository.deleteByUserIdAndRoomId(userId, roomId)
    }

    suspend fun doesRoomContainsMembers(roomId: String, members: Set<String>): Boolean {
        return membershipRepository.containsMembersByRoomId(roomId, members)
    }
}