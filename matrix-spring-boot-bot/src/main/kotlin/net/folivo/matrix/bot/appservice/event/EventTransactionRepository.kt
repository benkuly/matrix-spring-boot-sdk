package net.folivo.matrix.bot.appservice.event

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface EventTransactionRepository : ReactiveCrudRepository<EventTransaction, Long> {
    fun findByTnxIdAndEventIdType(tnxId: String, eventIdOrType: String): Mono<EventTransaction>
    fun existsByTnxIdAndEventIdOrType(tnxId: String, eventIdOrType: String): Mono<Boolean>
}