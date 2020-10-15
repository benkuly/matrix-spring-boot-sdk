package net.folivo.matrix.bot.client

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixSyncBatchTokenRepository : CoroutineCrudRepository<MatrixSyncBatchToken, String> {

    suspend fun findByUserId(userId: String): MatrixSyncBatchToken?
}