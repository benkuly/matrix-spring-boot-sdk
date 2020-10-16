package net.folivo.matrix.bot.appservice.event

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixEventTransactionRepository : CoroutineCrudRepository<MatrixEventTransaction, Long> {
    @Query(
            """
        SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
        FROM matrix_event_transaction
        WHERE tnx_id = :tnxId AND event_id = :eventId
    """
    )
    suspend fun existsByTnxIdAndEventId(tnxId: String, eventId: String): Boolean
}