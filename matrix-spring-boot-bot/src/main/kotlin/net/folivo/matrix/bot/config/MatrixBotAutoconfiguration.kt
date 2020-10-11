package net.folivo.matrix.bot.config

import net.folivo.matrix.appservice.config.AppserviceProperties
import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.bot.event.MatrixMessageHandler
import net.folivo.matrix.bot.event.MessageEventHandler
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.restclient.MatrixClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(MatrixBotProperties::class)
class MatrixBotAutoconfiguration {

    @Bean
    fun messageEventHandler(
            matrixMessageHandler: List<MatrixMessageHandler>,
            matrixClient: MatrixClient
    ): MatrixEventHandler {
        return MessageEventHandler(matrixMessageHandler, matrixClient)
    }

    @Bean
    fun botServiceHelper(
            botProperties: MatrixBotProperties,
            appserviceProperties: AppserviceProperties
    ): BotServiceHelper {
        return BotServiceHelper(botProperties, appserviceProperties)
    }

}