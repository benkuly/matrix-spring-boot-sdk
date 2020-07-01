package net.folivo.matrix.restclient.api.server

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class ServerApiClient(private val webClient: WebClient) {

    suspend fun getVersions(): VersionsResponse {
        return webClient
                .get().uri("/versions").retrieve()
                .awaitBody()
    }

    suspend fun getCapabilities(): CapabilitiesResponse {
        return webClient
                .get().uri("/r0/capabilities").retrieve()
                .awaitBody()
    }

}