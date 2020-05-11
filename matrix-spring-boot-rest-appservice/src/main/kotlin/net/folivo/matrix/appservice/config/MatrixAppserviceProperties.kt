package net.folivo.matrix.appservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties("matrix.appservice")
@ConstructorBinding
data class MatrixAppserviceProperties(
        val hsToken: String,
        val asUsername: String,
        val namespaces: Namespaces = Namespaces()
) {
    data class Namespaces(
            @DefaultValue("[]")
            val users: List<Namespace> = emptyList(),
            @DefaultValue("[]")
            val aliases: List<Namespace> = emptyList(),
            @DefaultValue("[]")
            val rooms: List<Namespace> = emptyList()
    )

    data class Namespace(
            @DefaultValue("true")
            val exclusive: Boolean = true,
            val regex: String
    )
}