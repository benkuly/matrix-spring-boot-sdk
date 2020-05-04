package net.folivo.matrix.bot.appservice.event

import net.folivo.matrix.bot.appservice.event.EventTransaction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EventTransactionRepository : CrudRepository<EventTransaction, Long> {
    fun findByTnxIdAndEventIdOrType(tnxId: String, eventIdOrType: String): EventTransaction?
}