package net.folivo.matrix.restclient.config

import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

class WebClientTokenAuthorizationFilter(private val matrixClientConfiguration: MatrixClientConfiguration) : ExchangeFilterFunction {
    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        val newRequest = ClientRequest.from(request)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${matrixClientConfiguration.token}")
                .build()
        return next.exchange(newRequest)
    }
}