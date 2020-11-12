package net.folivo.matrix.restclient.api.sync

import net.folivo.matrix.core.model.MatrixId.UserId

interface SyncBatchTokenService {
    suspend fun getBatchToken(userId: UserId? = null): String?
    suspend fun setBatchToken(value: String?, userId: UserId? = null)
}