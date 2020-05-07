package net.folivo.matrix.restclient.api.sync

import reactor.core.publisher.Mono

class InMemorySyncBatchTokenService(private var syncBatchToken: String? = null) : SyncBatchTokenService {
    override fun getBatchToken(): Mono<String> {
        return Mono.justOrEmpty(this.syncBatchToken)
    }

    override fun setBatchToken(value: String?): Mono<Void> {
        this.syncBatchToken = value
        return Mono.empty()
    }

}