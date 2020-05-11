package net.folivo.matrix.bot.appservice.event

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface EventTransactionRepository : ReactiveCrudRepository<EventTransaction, Long> {
    fun findByTnxIdAndEventIdElseType(tnxId: String, eventIdElseType: String): Mono<EventTransaction>
}