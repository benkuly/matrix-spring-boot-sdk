package net.folivo.matrix.restclient.api.sync

import com.github.michaelbull.retry.ContinueRetrying
import com.github.michaelbull.retry.policy.RetryPolicy
import com.github.michaelbull.retry.policy.binaryExponentialBackoff
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class SyncApiClient(private val webClient: WebClient, private val syncBatchTokenService: SyncBatchTokenService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun syncOnce(
            filter: String? = null,
            since: String? = null,
            fullState: Boolean = false,
            setPresence: Presence? = null,
            timeout: Long = 0
//            asUserId: String? = null // TODO currently not supported due to limit of syncBatchTokenService to only store one token
    ): SyncResponse {
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
                .awaitBody<SyncResponse>()
                .also { LOG.debug("synced with batchToken $since") }
    }

    private fun logAttempt(): RetryPolicy<Throwable> {
        return {
            LOG.error("error while sync to server: ${reason.message}")
            ContinueRetrying
        }
    }

    fun syncLoop(
            filter: String? = null,
            setPresence: Presence? = null
//            asUserId: String? = null // TODO see TODO in syncOnce parameter
    ): Flow<SyncResponse> {
        return flow {
            while (true) {
                val batchToken = syncBatchTokenService.getBatchToken()
                val response = retry(binaryExponentialBackoff(LongRange(1000, 90000)) + logAttempt()) {
                    if (batchToken != null) {
                        syncOnce(
                                filter = filter,
                                setPresence = setPresence,
                                fullState = false,
                                since = batchToken,
                                timeout = 30000
                                //asUserId = asUserId // TODO see TODO in syncOnce parameter
                        )
                    } else {
                        syncOnce(
                                filter = filter,
                                setPresence = setPresence,
                                fullState = false,
                                timeout = 30000
                                //asUserId = asUserId // TODO see TODO in syncOnce parameter
                        )
                    }
                }
                syncBatchTokenService.setBatchToken(response.nextBatch)
                emit(response)
            }
        }
    }

}