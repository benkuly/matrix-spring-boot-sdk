package net.folivo.matrix.bot.config

import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.appservice.config.MatrixAppserviceProperties
import net.folivo.matrix.bot.appservice.AppserviceBotManager
import net.folivo.matrix.bot.appservice.DefaultAppserviceBotManager
import net.folivo.matrix.bot.appservice.event.AutoJoinEventHandler
import net.folivo.matrix.bot.appservice.event.DefaultMatrixAppserviceEventService
import net.folivo.matrix.bot.appservice.event.EventTransactionRepository
import net.folivo.matrix.bot.appservice.room.AppserviceRoomRepository
import net.folivo.matrix.bot.appservice.room.DefaultMatrixAppserviceRoomService
import net.folivo.matrix.bot.appservice.user.AppserviceUserRepository
import net.folivo.matrix.bot.appservice.user.DefaultMatrixAppserviceUserService
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.restclient.MatrixClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories


@Configuration
@ConditionalOnProperty(prefix = "matrix.bot", name = ["mode"], havingValue = "APPSERVICE")
@EnableJpaRepositories("net.folivo.matrix.bot.appservice")
@EntityScan("net.folivo.matrix.bot.appservice")
class MatrixAppserviceBotAutoconfiguration(private val matrixBotProperties: MatrixBotProperties) {

    @Bean
    @ConditionalOnMissingBean
    fun defaultMatrixAppserviceEventService(
            matrixClient: MatrixClient,
            eventTransactionRepository: EventTransactionRepository,
            matrixEventHandler: List<MatrixEventHandler>
    ): MatrixAppserviceEventService {
        return DefaultMatrixAppserviceEventService(
                matrixClient,
                eventTransactionRepository,
                matrixEventHandler,
                matrixBotProperties.allowFederation,
                matrixBotProperties.serverName
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultAppserviceBotManager(): AppserviceBotManager {
        return DefaultAppserviceBotManager()
    }

    @Bean
    @ConditionalOnProperty(prefix = "matrix.bot", name = ["autoJoin"], havingValue = "true", matchIfMissing = true)
    fun autoJoinEventHandler(
            matrixClient: MatrixClient,
            defaultMatrixAppserviceRoomService: DefaultMatrixAppserviceRoomService,
            appserviceProperties: MatrixAppserviceProperties
    ): AutoJoinEventHandler {
        return AutoJoinEventHandler(
                matrixClient,
                defaultMatrixAppserviceRoomService,
                appserviceProperties.asUsername,
                appserviceProperties.namespaces.users.map { it.regex }
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultMatrixAppserviceUserService(
            appserviceUserRepository: AppserviceUserRepository,
            appserviceBotManager: AppserviceBotManager
    ): MatrixAppserviceUserService {
        return DefaultMatrixAppserviceUserService(appserviceBotManager, appserviceUserRepository)
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultMatrixAppserviceRoomService(
            appserviceRoomRepository: AppserviceRoomRepository,
            appserviceUserRepository: AppserviceUserRepository,
            appserviceBotManager: AppserviceBotManager
    ): DefaultMatrixAppserviceRoomService {
        return DefaultMatrixAppserviceRoomService(
                appserviceBotManager,
                appserviceRoomRepository,
                appserviceUserRepository
        )
    }
}