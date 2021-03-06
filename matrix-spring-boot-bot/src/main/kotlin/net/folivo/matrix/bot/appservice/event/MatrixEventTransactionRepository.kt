package net.folivo.matrix.bot.appservice.event

import net.folivo.matrix.core.model.MatrixId.EventId
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixEventTransactionRepository : CoroutineCrudRepository<MatrixEventTransaction, Long> {
    @Query(
            """
        SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
        FROM matrix_event_transaction t
        WHERE t.tnx_id = :tnxId AND t.event_id = :eventId
    """
    )
    suspend fun existsByTnxIdAndEventId(tnxId: String, eventId: EventId): Boolean
}