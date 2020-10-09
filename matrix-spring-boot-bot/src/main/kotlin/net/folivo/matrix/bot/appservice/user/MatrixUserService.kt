package net.folivo.matrix.bot.appservice.user

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.util.BotServiceHelper

class MatrixUserService(
        private val userRepository: MatrixUserRepository,
        private val helper: BotServiceHelper,
        private val matrixBotProperties: MatrixBotProperties
) {

    suspend fun getOrCreateUser(userId: String): MatrixUser {
        return userRepository.findById(userId).awaitFirstOrNull()
               ?: helper.isManagedUser(userId)
                       .let { userRepository.save(MatrixUser(userId, it)).awaitFirst() }
    }

    suspend fun deleteUser(userId: String) {
        userRepository.deleteById(userId).awaitFirst()
    }

    suspend fun deleteAllUsers() {
        userRepository.deleteAll().awaitFirstOrNull()
    }

    suspend fun existsUser(userId: String): Boolean {
        return userRepository.existsById(userId).awaitFirst()
    }

    fun getUsers(roomId: String): Flow<MatrixUser> {
        return userRepository.findByRoomId(roomId).asFlow()
    }
}