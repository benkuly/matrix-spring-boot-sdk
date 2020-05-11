package net.folivo.matrix.bot.client

import net.folivo.matrix.restclient.api.sync.SyncBatchTokenService
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

class PersistenceSyncBatchTokenService(private val syncBatchTokenRepository: SyncBatchTokenRepository) : SyncBatchTokenService {

    private val logger = LoggerFactory.getLogger(PersistenceSyncBatchTokenService::class.java)

    companion object {
        private const val BATCH_TOKEN_ID = "default"
    }

    override fun getBatchToken(): Mono<String> {
        return syncBatchTokenRepository.findById(BATCH_TOKEN_ID)
                .switchIfEmpty(syncBatchTokenRepository.save(SyncBatchToken(BATCH_TOKEN_ID)))
                .doOnSuccess { logger.debug("getBatchToken returns ${it.value}") }
                .flatMap { Mono.justOrEmpty(it.value) }
    }

    override fun setBatchToken(value: String?): Mono<Void> {
        return syncBatchTokenRepository.findById(BATCH_TOKEN_ID)
                .switchIfEmpty(syncBatchTokenRepository.save(SyncBatchToken(BATCH_TOKEN_ID, value)))
                .flatMap {
                    syncBatchTokenRepository.save(it.copy(value = value))
                }.doOnSuccess { logger.debug("setBatchToken ${it.value}") }
                .then()
    }
}