package net.folivo.matrix.restclient.api.sync

interface SyncBatchTokenService {
    suspend fun getBatchToken(userId: String? = null): String?
    suspend fun setBatchToken(value: String?, userId: String? = null)
}