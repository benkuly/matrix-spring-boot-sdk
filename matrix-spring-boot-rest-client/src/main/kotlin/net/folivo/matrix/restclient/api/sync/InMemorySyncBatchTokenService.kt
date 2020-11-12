package net.folivo.matrix.restclient.api.sync

import net.folivo.matrix.core.model.MatrixId.UserId

class InMemorySyncBatchTokenService(
        private val syncBatchTokenMap: MutableMap<UserId, String?> = mutableMapOf()
) : SyncBatchTokenService {
    override suspend fun getBatchToken(userId: UserId?): String? {
        return this.syncBatchTokenMap[userId ?: UserId("@default:server")]
    }

    override suspend fun setBatchToken(value: String?, userId: UserId?) {
        syncBatchTokenMap[userId ?: UserId("@default:server")] = value
    }

}