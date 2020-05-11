package net.folivo.matrix.bot.client

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SyncBatchTokenRepository : ReactiveCrudRepository<SyncBatchToken, String> {
}