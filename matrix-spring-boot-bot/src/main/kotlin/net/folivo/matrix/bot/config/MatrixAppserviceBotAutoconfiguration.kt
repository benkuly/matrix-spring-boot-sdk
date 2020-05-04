package net.folivo.matrix.bot.config

import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.bot.appservice.AppserviceBotManager
import net.folivo.matrix.bot.appservice.event.DefaultMatrixAppserviceEventService
import net.folivo.matrix.bot.appservice.event.EventTransactionRepository
import net.folivo.matrix.bot.appservice.user.AppserviceUserRepository
import net.folivo.matrix.bot.appservice.user.DefaultMatrixAppserviceUserService
import net.folivo.matrix.core.handler.MatrixEventHandler
import net.folivo.matrix.restclient.config.MatrixClientConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories


@Configuration
@AutoConfigureAfter(MatrixClientConfiguration::class)
@ConditionalOnProperty(prefix = "matrix.bot", name = ["mode"], havingValue = "APPSERVICE")
@EnableJpaRepositories("net.folivo.matrix.bot.appservice")
@EntityScan("net.folivo.matrix.bot.appservice")
class MatrixAppserviceBotAutoconfiguration(private val botProperties: MatrixBotProperties) {

    @Bean
    @ConditionalOnMissingBean
    fun defaultMatrixAppserviceEventService(
            eventTransactionRepository: EventTransactionRepository,
            matrixEventHandler: List<MatrixEventHandler>
    ): MatrixAppserviceEventService {
        return DefaultMatrixAppserviceEventService(
                eventTransactionRepository,
                matrixEventHandler
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun noopMatrixAppserviceUserService(
            appserviceUserRepository: AppserviceUserRepository,
            appserviceBotManager: AppserviceBotManager
    ): MatrixAppserviceUserService {
        return DefaultMatrixAppserviceUserService(appserviceBotManager, appserviceUserRepository)
    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    fun noOpMatrixAppserviceRoomService(): MatrixAppserviceRoomService {
//        return NoOpMatrixAppserviceRoomService()
//    }
}