package net.folivo.matrix.bot.appservice.event

import kotlinx.coroutines.reactive.awaitFirst

class MatrixEventTransactionService(private val eventTransactionRepository: MatrixEventTransactionRepository) {

    suspend fun hasEvent(tnxId: String, eventIdOrHash: String): Boolean {
        return eventTransactionRepository.existsByTnxIdAndEventId(tnxId, eventIdOrHash).awaitFirst()
    }

    suspend fun saveEvent(event: MatrixEventTransaction): MatrixEventTransaction {
        return eventTransactionRepository.save(event).awaitFirst()
    }
}