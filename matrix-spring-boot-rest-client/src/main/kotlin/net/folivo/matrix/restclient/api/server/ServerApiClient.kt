package net.folivo.matrix.restclient.api.server

import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

class ServerApiClient(private val webClient: WebClient) {

    fun getVersions(): Mono<VersionsResponse> {
        return webClient
                .get().uri("/versions").retrieve()
                .bodyToMono(VersionsResponse::class.java)
    }

    fun getCapabilities(): Mono<CapabilitiesResponse> {
        return webClient
                .get().uri("/r0/capabilities").retrieve()
                .bodyToMono(CapabilitiesResponse::class.java)
    }

}