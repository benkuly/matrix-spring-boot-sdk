package net.folivo.spring.matrix.appservice

import net.folivo.trixnity.appservice.rest.MatrixAppserviceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("matrix.appservice")
@ConstructorBinding
data class MatrixAppserviceConfigurationProperties(
    val hsToken: String,
    val port: Int = 8080,
    val namespaces: Namespaces = Namespaces()
) {
    data class Namespaces(
        val users: List<Namespace> = emptyList(),
        val aliases: List<Namespace> = emptyList(),
        val rooms: List<Namespace> = emptyList()
    )

    data class Namespace(
        val localpartRegex: String
    )

    fun toMatrixAppserviceProperties(): MatrixAppserviceProperties {
        return MatrixAppserviceProperties(hsToken)
    }
}