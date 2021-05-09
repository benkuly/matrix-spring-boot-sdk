package net.folivo.spring.matrix.bot.appservice.event

class MatrixEventTransactionService(private val eventTransactionRepository: MatrixEventTransactionRepository) {

    suspend fun hasTransaction(tnxId: String): Boolean {
        return eventTransactionRepository.existsById(tnxId)
    }

    suspend fun saveTransaction(eventTransaction: MatrixEventTransaction): MatrixEventTransaction {
        return eventTransactionRepository.save(eventTransaction)
    }
}