package net.folivo.spring.matrix.bot.appservice.event

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixEventTransactionRepository : CoroutineCrudRepository<MatrixEventTransaction, String> {
}