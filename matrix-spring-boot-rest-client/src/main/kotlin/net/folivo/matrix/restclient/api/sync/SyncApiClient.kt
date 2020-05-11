package net.folivo.matrix.restclient.api.sync

import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class SyncApiClient(private val webClient: WebClient, private val syncBatchTokenService: SyncBatchTokenService) {

    private val logger = LoggerFactory.getLogger(SyncApiClient::class.java)

    fun syncOnce(
            filter: String? = null,
            since: String? = null,
            fullState: Boolean = false,
            setPresence: Presence? = null,
            timeout: Long = 0
//            asUserId: String? = null // TODO currently not supported due to limit of syncBatchTokenService to only store one token
    ): Mono<SyncResponse> {
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/sync")
                        if (filter != null) queryParam("filter", filter)
                        queryParam("full_state", fullState)
                        if (setPresence != null) queryParam("set_presence", setPresence.value)
                        if (since != null) queryParam("since", since)
                        queryParam("timeout", timeout)
//                        if (asUserId != null) queryParam("user_id", asUserId) // TODO see TODO in syncOnce parameter
                    }.build()
                }
                .retrieve()
                .bodyToMono(SyncResponse::class.java)
                .doOnSuccess { logger.debug("synced with batchToken $since") }
    }

    fun syncLoop(
            filter: String? = null,
            setPresence: Presence? = null
//            asUserId: String? = null // TODO see TODO in syncOnce parameter
    ): Flux<SyncResponse> {
        return Mono.just(true) // TODO is there a less hacky way? Without that line, repeat does not call getBatchToken
                .flatMap {
                    syncBatchTokenService.getBatchToken()
                }.flatMap { batchToken ->
                    syncOnce(
                            filter = filter,
                            setPresence = setPresence,
                            fullState = false,
                            since = batchToken,
                            timeout = 30000
                            //asUserId = asUserId // TODO see TODO in syncOnce parameter
                    )
                }.switchIfEmpty(
                        syncOnce(
                                filter = filter,
                                setPresence = setPresence,
                                fullState = false,
                                timeout = 30000
                                //asUserId = asUserId // TODO see TODO in syncOnce parameter
                        )
                ).flatMap { response ->
                    if (response != null) {
                        syncBatchTokenService.setBatchToken(response.nextBatch)
                                .thenReturn(response)
                    } else {
                        Mono.just<SyncResponse>(response)
                    }
                }
                .repeat()
                .retry()
                .doOnError { logger.error("error in syncLoop", it) }
    }

}