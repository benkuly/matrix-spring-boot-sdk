package net.folivo.matrix.restclient.api.sync

interface SyncBatchTokenService {
    suspend fun getBatchToken(): String?
    suspend fun setBatchToken(value: String?)
}