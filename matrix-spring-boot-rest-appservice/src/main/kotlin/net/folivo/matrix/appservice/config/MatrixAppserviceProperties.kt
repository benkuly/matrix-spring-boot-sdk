package net.folivo.matrix.appservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("matrix.appservice")
@ConstructorBinding
data class MatrixAppserviceProperties(
        val hsToken: String
)