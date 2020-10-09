package net.folivo.matrix.bot.appservice.membership

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MatrixMembershipService(private val membershipRepository: MatrixMembershipRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun getOrCreateMembership(userId: String, roomId: String): MatrixMembership {//FIXME test
        return membershipRepository.findByUserIdAndRoomId(userId, roomId).awaitFirstOrNull()
               ?: membershipRepository.save(MatrixMembership(userId, roomId)).awaitFirst()
    }

    suspend fun getMembershipsByRoomId(roomId: String): Flow<MatrixMembership> {
        return membershipRepository.findByRoomId(roomId).asFlow()
    }

    suspend fun getMembershipsByUserId(userId: String): Flow<MatrixMembership> {
        return membershipRepository.findByUserId(userId).asFlow()
    }

    suspend fun getMembershipsSizeByUserId(userId: String): Long {
        return membershipRepository.countByUserId(userId).awaitFirst()
    }

    suspend fun getMembershipsSizeByRoomId(roomId: String): Long {
        return membershipRepository.countByRoomId(roomId).awaitFirst()
    }

    suspend fun hasRoomOnlyManagedUsersLeft(roomId: String): Boolean {
        return membershipRepository.containsOnlyManagedMembersByRoomId(roomId).awaitFirst()
    }

    suspend fun deleteMembership(userId: String, roomId: String) {
        membershipRepository.deleteByUserIdAndRoomId(userId, roomId).awaitFirstOrNull()
    }

    suspend fun doesRoomContainsMembers(roomId: String, members: Set<String>): Boolean {
        return membershipRepository.containsMembersByRoomId(roomId, members).awaitFirst()
    }
}