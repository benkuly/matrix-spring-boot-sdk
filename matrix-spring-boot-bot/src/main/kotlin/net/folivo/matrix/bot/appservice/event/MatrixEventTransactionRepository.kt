package net.folivo.matrix.bot.appservice.event

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface MatrixEventTransactionRepository : ReactiveCrudRepository<MatrixEventTransaction, Long> {
    fun existsByTnxIdAndEventId(tnxId: String, eventId: String): Mono<Boolean>
}