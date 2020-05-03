package net.folivo.matrix.bot.config

import net.folivo.matrix.bot.handler.MatrixMessageContentHandler
import net.folivo.matrix.bot.handler.MatrixMessageEventHandler
import net.folivo.matrix.core.handler.MatrixEventHandler
import net.folivo.matrix.restclient.MatrixClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(MatrixBotProperties::class)
class MatrixBotAutoconfiguration(private val botProperties: MatrixBotProperties) {

    @Bean
    fun matrixMessageEventHandler(
            matrixMessageContentHandler: List<MatrixMessageContentHandler>,
            matrixClient: MatrixClient
    ): MatrixEventHandler {
        return MatrixMessageEventHandler(matrixMessageContentHandler, matrixClient)
    }

}