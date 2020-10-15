package net.folivo.matrix.restclient.api.sync

class InMemorySyncBatchTokenService(
        private val syncBatchTokenMap: MutableMap<String, String?> = mutableMapOf()
) : SyncBatchTokenService { // FIXME test
    override suspend fun getBatchToken(userId: String?): String? {
        return this.syncBatchTokenMap[userId ?: "default"]
    }

    override suspend fun setBatchToken(value: String?, userId: String?) {
        this.syncBatchTokenMap[userId ?: "default"] = value
    }

}