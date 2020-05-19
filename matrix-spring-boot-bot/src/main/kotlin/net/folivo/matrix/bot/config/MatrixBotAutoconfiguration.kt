package net.folivo.matrix.bot.config

import net.folivo.matrix.bot.handler.*
import net.folivo.matrix.restclient.MatrixClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(MatrixBotProperties::class)
class MatrixBotAutoconfiguration(private val botProperties: MatrixBotProperties) {

    @Bean
    @ConditionalOnMissingBean // TODO not when autoJoin DISABLED
    fun defaultAutoJoinService(): AutoJoinService {
        return DefaultAutoJoinService()
    }

    @Bean
    fun matrixMessageEventHandler(
            matrixMessageContentHandler: List<MatrixMessageContentHandler>,
            matrixClient: MatrixClient
    ): MatrixEventHandler {
        return MatrixMessageEventHandler(matrixMessageContentHandler, matrixClient)
    }

}