package net.folivo.matrix.bot.client

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SyncBatchTokenRepository : CrudRepository<SyncBatchToken, Int> {
}