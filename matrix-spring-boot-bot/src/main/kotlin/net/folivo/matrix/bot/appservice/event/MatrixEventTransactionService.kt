package net.folivo.matrix.bot.appservice.event

class MatrixEventTransactionService(private val eventTransactionRepository: MatrixEventTransactionRepository) {

    suspend fun hasEvent(tnxId: String, eventIdOrHash: String): Boolean {
        return eventTransactionRepository.existsByTnxIdAndEventId(tnxId, eventIdOrHash)
    }

    suspend fun saveEvent(event: MatrixEventTransaction): MatrixEventTransaction {
        return eventTransactionRepository.save(event)
    }
}