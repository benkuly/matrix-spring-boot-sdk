package net.folivo.matrix.bot.user

import kotlinx.coroutines.flow.Flow
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId

class MatrixUserService(
        private val userRepository: MatrixUserRepository,
        private val helper: BotServiceHelper
) {

    suspend fun getOrCreateUser(userId: UserId): MatrixUser {
        return userRepository.findById(userId)
               ?: userRepository.save(MatrixUser(userId, helper.isManagedUser(userId)))
    }

    suspend fun deleteUser(userId: UserId) {
        userRepository.deleteById(userId)
    }

    suspend fun deleteAllUsers() {
        userRepository.deleteAll()
    }

    suspend fun existsUser(userId: UserId): Boolean {
        return userRepository.existsById(userId)
    }

    fun getUsersByRoom(roomId: RoomId): Flow<MatrixUser> {
        return userRepository.findByRoomId(roomId)
    }
}