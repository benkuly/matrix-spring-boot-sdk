package net.folivo.matrix.appservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("matrix.appservice")
@ConstructorBinding
data class AppserviceProperties(
        val hsToken: String,
        val namespaces: Namespaces = Namespaces()
) {
    data class Namespaces(
            val users: List<Namespace> = emptyList(),
            val aliases: List<Namespace> = emptyList(),
            val rooms: List<Namespace> = emptyList()
    )

    data class Namespace(
            val exclusive: Boolean = true,
            val regex: String
    )
}