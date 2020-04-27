package net.folivo.matrix.appservice.config

import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono

class MatrixHomeServerAuthenticationManager(private val hsToken: String) : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val credentials = authentication.credentials
        if (credentials is String && credentials == hsToken) {
            authentication.isAuthenticated = true
            return Mono.just(authentication)
        }
        return Mono.empty()
    }
}