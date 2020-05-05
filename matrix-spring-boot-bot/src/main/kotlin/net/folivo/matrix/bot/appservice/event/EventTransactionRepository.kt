package net.folivo.matrix.bot.appservice.event

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EventTransactionRepository : CrudRepository<EventTransaction, Long> {
    fun findByTnxIdAndEventIdType(tnxId: String, eventIdOrType: String): EventTransaction?
}