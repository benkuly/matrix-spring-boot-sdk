package net.folivo.spring.matrix.bot.event

import net.folivo.trixnity.core.model.MatrixId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixSyncBatchTokenRepository : CoroutineCrudRepository<MatrixSyncBatchToken, MatrixId.UserId> {

    suspend fun findByUserId(userId: MatrixId.UserId): MatrixSyncBatchToken?
}