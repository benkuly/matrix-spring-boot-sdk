package net.folivo.matrix.appservice.config

import org.springframework.security.authentication.AbstractAuthenticationToken

class MatrixHomeServerAuthenticationToken(private val accessToken: String) : AbstractAuthenticationToken(emptyList()) {

    override fun getCredentials(): Any {
        return accessToken
    }

    override fun getPrincipal(): Any {
        return accessToken
    }
}