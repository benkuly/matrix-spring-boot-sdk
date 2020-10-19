package net.folivo.matrix.bot.client

import net.folivo.matrix.core.model.MatrixId.UserId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixSyncBatchTokenRepository : CoroutineCrudRepository<MatrixSyncBatchToken, UserId> {

    suspend fun findByUserId(userId: UserId): MatrixSyncBatchToken?
}