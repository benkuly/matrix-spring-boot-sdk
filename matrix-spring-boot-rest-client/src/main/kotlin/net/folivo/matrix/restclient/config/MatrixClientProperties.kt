package net.folivo.matrix.restclient.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("matrix.client")
@ConstructorBinding
data class MatrixClientProperties(
        val homeServer: MatrixHomeServerProperties,
        val token: String?
) {
    data class MatrixHomeServerProperties(
            val hostname: String,
            val port: Int = 443,
            val secure: Boolean = true
    )
}