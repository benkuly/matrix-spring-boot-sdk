package net.folivo.matrix.bot.appservice.event

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface MatrixEventTransactionRepository : ReactiveCrudRepository<MatrixEventTransaction, Long> {
    fun containsByTnxIdAndEventIdOrHash(tnxId: String, eventIdOrHash: String): Mono<Boolean>
}