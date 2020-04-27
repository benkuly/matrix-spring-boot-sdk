package net.folivo.matrix.appservice.config

import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class MatrixHomeServerAuthenticationConverter : ServerAuthenticationConverter {
    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.justOrEmpty(exchange.request.queryParams.getFirst("access_token")?.let {
            MatrixHomeServerAuthenticationToken(it)
        })
    }
}