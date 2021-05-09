package net.folivo.spring.matrix.restclient

import net.folivo.trixnity.client.rest.MatrixClientProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("matrix.client")
@ConstructorBinding
data class MatrixClientConfigurationProperties(
    val homeServer: MatrixHomeServerConfigurationProperties,
    val token: String?
) {
    data class MatrixHomeServerConfigurationProperties(
        val hostname: String,
        val port: Int = 443,
        val secure: Boolean = true
    )

    fun toMatrixClientProperties(): MatrixClientProperties {
        return MatrixClientProperties(
            MatrixClientProperties.MatrixHomeServerProperties(
                homeServer.hostname,
                homeServer.port,
                homeServer.secure
            ), token
        )
    }
}