package net.folivo.matrix.bot.appservice.event

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixEventTransactionRepository : CoroutineCrudRepository<MatrixEventTransaction, Long> {
    suspend fun existsByTnxIdAndEventId(tnxId: String, eventId: String): Boolean
}