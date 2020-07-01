package net.folivo.matrix.restclient.api.sync

class InMemorySyncBatchTokenService(private var syncBatchToken: String? = null) : SyncBatchTokenService {
    override suspend fun getBatchToken(): String? {
        return this.syncBatchToken
    }

    override suspend fun setBatchToken(value: String?) {
        this.syncBatchToken = value
    }

}