package net.folivo.matrix.restclient.api.sync

import reactor.core.publisher.Mono

interface SyncBatchTokenService {
    fun getBatchToken(): Mono<String>
    fun setBatchToken(value: String?): Mono<Void>
}