package net.folivo.matrix.bot.client

import net.folivo.matrix.core.model.MatrixUserId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixSyncBatchTokenRepository : CoroutineCrudRepository<MatrixSyncBatchToken, MatrixUserId> {

    suspend fun findByUserId(userId: String): MatrixSyncBatchToken?
}