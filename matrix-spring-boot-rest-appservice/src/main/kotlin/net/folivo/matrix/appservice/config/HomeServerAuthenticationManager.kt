package net.folivo.matrix.appservice.config

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono

class HomeServerAuthenticationManager(private val hsToken: String) : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val credentials = authentication.credentials
        return if (credentials is String && credentials == hsToken) {
            authentication.isAuthenticated = true
            Mono.just(authentication)
        } else {
            Mono.error(BadCredentialsException("invalid access_token"))
        }
    }
}