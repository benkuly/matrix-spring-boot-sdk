package net.folivo.matrix.restclient.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties("matrix.client")
@ConstructorBinding
data class MatrixClientProperties(val homeServer: MatrixHomeServerProperties, val token: String) {
    data class MatrixHomeServerProperties(
            val hostname: String,
            @DefaultValue("443") val port: Int,
            @DefaultValue("true") val secure: Boolean
    )
}