package net.folivo.spring.matrix.bot.user

import kotlinx.coroutines.flow.Flow
import net.folivo.spring.matrix.bot.util.BotServiceHelper
import net.folivo.trixnity.core.model.MatrixId

class MatrixUserService(
    private val userRepository: MatrixUserRepository,
    private val helper: BotServiceHelper
) {

    suspend fun getOrCreateUser(userId: MatrixId.UserId): MatrixUser {
        return userRepository.findById(userId)
            ?: userRepository.save(MatrixUser(userId, helper.isManagedUser(userId)))
    }

    suspend fun deleteUser(userId: MatrixId.UserId) {
        userRepository.deleteById(userId)
    }

    suspend fun deleteAllUsers() {
        userRepository.deleteAll()
    }

    suspend fun existsUser(userId: MatrixId.UserId): Boolean {
        return userRepository.existsById(userId)
    }

    fun getUsersByRoom(roomId: MatrixId.RoomId): Flow<MatrixUser> {
        return userRepository.findByRoomId(roomId)
    }
}