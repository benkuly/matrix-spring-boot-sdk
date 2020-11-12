package net.folivo.matrix.bot.event

import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.user.MatrixUserService
import net.folivo.matrix.core.model.MatrixId.UserId
import net.folivo.matrix.restclient.api.sync.SyncBatchTokenService

class PersistentSyncBatchTokenService(
        private val syncBatchTokenRepository: MatrixSyncBatchTokenRepository,
        private val userService: MatrixUserService,
        private val botProperties: MatrixBotProperties
) : SyncBatchTokenService {

    override suspend fun getBatchToken(userId: UserId?): String? {
        return syncBatchTokenRepository.findByUserId(userId ?: botProperties.botUserId)?.token
    }

    override suspend fun setBatchToken(value: String?, userId: UserId?) {
        val realUserId = userId ?: botProperties.botUserId
        val token = syncBatchTokenRepository.findByUserId(realUserId)
        if (token != null) {
            syncBatchTokenRepository.save(token.copy(token = value))
        } else {
            userService.getOrCreateUser(realUserId)
            syncBatchTokenRepository.save(MatrixSyncBatchToken(realUserId, value))
        }
    }
}