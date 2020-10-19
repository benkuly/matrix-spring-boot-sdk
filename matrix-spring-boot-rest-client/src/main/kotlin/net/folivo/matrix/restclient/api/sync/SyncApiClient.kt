package net.folivo.matrix.restclient.api.sync

import com.github.michaelbull.retry.ContinueRetrying
import com.github.michaelbull.retry.policy.RetryPolicy
import com.github.michaelbull.retry.policy.binaryExponentialBackoff
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.folivo.matrix.core.model.MatrixId.UserId
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class SyncApiClient(
        private val webClient: WebClient,
        private val syncBatchTokenService: SyncBatchTokenService
) { //FIXME test asUserId

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun syncOnce(
            filter: String? = null,
            since: String? = null,
            fullState: Boolean = false,
            setPresence: Presence? = null,
            timeout: Long = 0,
            asUserId: UserId? = null
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
                        if (asUserId != null) queryParam("user_id", asUserId.full)
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
            setPresence: Presence? = null,
            asUserId: UserId? = null
    ): Flow<SyncResponse> {
        return flow {
            while (true) {
                val batchToken = syncBatchTokenService.getBatchToken(asUserId)
                val response = retry(binaryExponentialBackoff(LongRange(1000, 90000)) + logAttempt()) {
                    if (batchToken != null) {
                        syncOnce(
                                filter = filter,
                                setPresence = setPresence,
                                fullState = false,
                                since = batchToken,
                                timeout = 30000,
                                asUserId = asUserId
                        )
                    } else {
                        syncOnce(
                                filter = filter,
                                setPresence = setPresence,
                                fullState = false,
                                timeout = 30000,
                                asUserId = asUserId
                        )
                    }
                }
                syncBatchTokenService.setBatchToken(response.nextBatch, asUserId)
                emit(response)
            }
        }
    }

}