package net.folivo.matrix.bot.config

import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.bot.event.MatrixMessageEventHandler
import net.folivo.matrix.bot.event.MatrixMessageHandler
import net.folivo.matrix.bot.membership.AutoJoinCustomizer
import net.folivo.matrix.bot.membership.DefaultAutoJoinCustomizer
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
    fun defaultAutoJoinService(): AutoJoinCustomizer {
        return DefaultAutoJoinCustomizer()
    }

    @Bean
    fun matrixMessageEventHandler(
            matrixMessageHandler: List<MatrixMessageHandler>,
            matrixClient: MatrixClient
    ): MatrixEventHandler {
        return MatrixMessageEventHandler(matrixMessageHandler, matrixClient)
    }

}