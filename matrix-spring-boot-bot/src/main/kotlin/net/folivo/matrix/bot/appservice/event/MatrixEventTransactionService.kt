package net.folivo.matrix.bot.appservice.event

import net.folivo.matrix.core.model.MatrixId.EventId

class MatrixEventTransactionService(private val eventTransactionRepository: MatrixEventTransactionRepository) {

    suspend fun hasEvent(tnxId: String, eventId: EventId): Boolean {
        return eventTransactionRepository.existsByTnxIdAndEventId(tnxId, eventId)
    }

    suspend fun saveEvent(event: MatrixEventTransaction): MatrixEventTransaction {
        return eventTransactionRepository.save(event)
    }
}