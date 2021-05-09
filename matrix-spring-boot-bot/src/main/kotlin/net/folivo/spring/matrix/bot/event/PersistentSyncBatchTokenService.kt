package net.folivo.spring.matrix.bot.event

import net.folivo.spring.matrix.bot.config.MatrixBotProperties
import net.folivo.spring.matrix.bot.user.MatrixUserService
import net.folivo.trixnity.client.rest.api.sync.SyncBatchTokenService
import net.folivo.trixnity.core.model.MatrixId

class PersistentSyncBatchTokenService(
    private val syncBatchTokenRepository: MatrixSyncBatchTokenRepository,
    private val userService: MatrixUserService,
    private val botProperties: MatrixBotProperties
) : SyncBatchTokenService {

    override suspend fun getBatchToken(userId: MatrixId.UserId?): String? {
        return syncBatchTokenRepository.findByUserId(userId ?: botProperties.botUserId)?.token
    }

    override suspend fun setBatchToken(value: String?, userId: MatrixId.UserId?) {
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