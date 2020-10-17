package net.folivo.matrix.bot.client

import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.restclient.api.sync.SyncBatchTokenService

class PersistentSyncBatchTokenService(
        private val syncBatchTokenRepository: MatrixSyncBatchTokenRepository,
        private val helper: BotServiceHelper
) : SyncBatchTokenService {

    override suspend fun getBatchToken(userId: String?): String? {
        return syncBatchTokenRepository.findByUserId(userId ?: helper.getBotUserId())?.token
    }

    override suspend fun setBatchToken(value: String?, userId: String?) {
        val realUserId = userId ?: helper.getBotUserId()
        val token = syncBatchTokenRepository.findByUserId(realUserId)
        if (token != null) {
            syncBatchTokenRepository.save(token.copy(token = value))
        } else {
            syncBatchTokenRepository.save(MatrixSyncBatchToken(realUserId, value))
        }
    }
}