package net.folivo.matrix.bot.client

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface MatrixSyncBatchTokenRepository : ReactiveCrudRepository<MatrixSyncBatchToken, String> {

    fun findByUserId(userId: String): Mono<MatrixSyncBatchToken>
}