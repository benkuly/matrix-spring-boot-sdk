package net.folivo.matrix.restclient.api.sync

import org.slf4j.LoggerFactory
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink

class SyncApiClient(private val webClient: WebClient, private val syncBatchTokenService: SyncBatchTokenService) {

    private val logger = LoggerFactory.getLogger(SyncApiClient::class.java)

    fun syncOnce(
            filter: String? = null,
            since: String? = null,
            fullState: Boolean? = false,
            setPresence: Presence? = null,
            timeout: Long = 0
    ): Mono<SyncResponse> {
        val params = LinkedMultiValueMap<String, String>();
        filter?.also { params.add("filter", it) }
        fullState.also { params.add("full_state", it.toString()) }
        setPresence?.also { params.add("set_presence", it.value) }
        since?.also { params.add("since", it) }
        timeout.also { params.add("timeout", it.toString()) }
        return webClient
                .get().uri { it.path("/r0/sync").queryParams(params).build() }
                .retrieve()
                .bodyToMono(SyncResponse::class.java)
    }

    fun syncLoop(
            filter: String? = null,
            setPresence: Presence? = null
    ): Flux<SyncResponse> {
        return Flux.generate(
                { syncBatchTokenService.batchToken },
                { state: String?, sink: SynchronousSink<SyncResponse> -> // TODO maybe asynchronous sink?
                    val syncResponse = syncOnce(
                            filter = filter,
                            setPresence = setPresence,
                            fullState = false,
                            since = state,
                            timeout = 30000
                    ).block()
                    logger.debug("synced (token: $state)")
                    if (syncResponse != null) {
                        sink.next(syncResponse)
                        val (nextBatch) = syncResponse
                        syncBatchTokenService.batchToken = nextBatch
                        nextBatch
                    } else {
                        state
                    }
                }
        )
    }

}