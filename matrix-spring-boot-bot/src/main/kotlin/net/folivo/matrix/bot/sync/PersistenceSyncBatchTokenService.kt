package net.folivo.matrix.bot.sync

import net.folivo.matrix.restclient.api.sync.SyncBatchTokenService
import org.springframework.data.repository.findByIdOrNull

class PersistenceSyncBatchTokenService(private val syncBatchTokenRepository: SyncBatchTokenRepository) : SyncBatchTokenService {
    override var batchToken: String?
        get() {
            val token = syncBatchTokenRepository.findByIdOrNull(1)
            return if (token == null) {
                syncBatchTokenRepository.save(SyncBatchToken(1)).value
            } else {
                token.value
            }
        }
        set(value) {
            val token = syncBatchTokenRepository.findByIdOrNull(1)
            if (token == null) {
                syncBatchTokenRepository.save(SyncBatchToken(1, value))
            } else {
                token.value = value
                syncBatchTokenRepository.save(token)
            }
        }
}