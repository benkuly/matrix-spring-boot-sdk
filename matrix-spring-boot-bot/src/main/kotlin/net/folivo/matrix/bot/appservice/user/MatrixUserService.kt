package net.folivo.matrix.bot.appservice.user

import kotlinx.coroutines.flow.Flow
import net.folivo.matrix.bot.util.BotServiceHelper

class MatrixUserService(
        private val userRepository: MatrixUserRepository,
        private val helper: BotServiceHelper
) {

    suspend fun getOrCreateUser(userId: String): MatrixUser {
        return userRepository.findById(userId)
               ?: userRepository.save(MatrixUser(userId, helper.isManagedUser(userId)))
    }

    suspend fun deleteUser(userId: String) {
        userRepository.deleteById(userId)
    }

    suspend fun deleteAllUsers() {
        userRepository.deleteAll()
    }

    suspend fun existsUser(userId: String): Boolean {
        return userRepository.existsById(userId)
    }

    fun getUsers(roomId: String): Flow<MatrixUser> {
        return userRepository.findByRoomId(roomId)
    }
}