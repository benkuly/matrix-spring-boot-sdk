package net.folivo.spring.matrix.bot.config

import net.folivo.spring.matrix.appservice.MatrixAppserviceAutoconfiguration
import net.folivo.spring.matrix.appservice.MatrixAppserviceConfigurationProperties
import net.folivo.spring.matrix.bot.appservice.AppserviceMemberEventHandler
import net.folivo.spring.matrix.bot.appservice.BotUserInitializer
import net.folivo.spring.matrix.bot.appservice.DefaultAppserviceRoomService
import net.folivo.spring.matrix.bot.appservice.DefaultAppserviceUserService
import net.folivo.spring.matrix.bot.appservice.event.DefaultAppserviceEventTnxService
import net.folivo.spring.matrix.bot.appservice.event.MatrixEventTransactionRepository
import net.folivo.spring.matrix.bot.appservice.event.MatrixEventTransactionService
import net.folivo.spring.matrix.bot.appservice.sync.InitialSyncService
import net.folivo.spring.matrix.bot.event.EventHandlerRunner
import net.folivo.spring.matrix.bot.event.MatrixEventHandler
import net.folivo.spring.matrix.bot.membership.MatrixMembershipSyncService
import net.folivo.spring.matrix.bot.membership.MembershipChangeHandler
import net.folivo.spring.matrix.bot.room.MatrixRoomService
import net.folivo.spring.matrix.bot.user.MatrixUserService
import net.folivo.spring.matrix.bot.util.BotServiceHelper
import net.folivo.trixnity.appservice.rest.DefaultAppserviceService
import net.folivo.trixnity.appservice.rest.event.AppserviceEventTnxService
import net.folivo.trixnity.appservice.rest.room.AppserviceRoomService
import net.folivo.trixnity.appservice.rest.user.AppserviceUserService
import net.folivo.trixnity.client.rest.MatrixClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile


@Configuration
@ConditionalOnProperty(prefix = "matrix.bot", name = ["mode"], havingValue = "APPSERVICE")
class MatrixAppserviceBotAutoconfiguration {

    @Configuration
    @Profile("initialsync")
    inner class InitialSyncConfiguration {
        @Bean
        fun initialSyncService(
            userService: MatrixUserService,
            roomService: MatrixRoomService,
            membershipSyncService: MatrixMembershipSyncService
        ): InitialSyncService {
            return InitialSyncService(userService, roomService, membershipSyncService)
        }
    }

    @Configuration
    @Profile("!initialsync")
    @Import(MatrixAppserviceAutoconfiguration::class)
    inner class NotInitialSyncConfiguration {

        @ConditionalOnBean(DefaultAppserviceService::class)
        @Bean
        fun eventHandlerRunner(
            appserviceService: DefaultAppserviceService,
            eventHandler: List<MatrixEventHandler<*>>,
        ): EventHandlerRunner {
            return EventHandlerRunner(
                appserviceService,
                eventHandler
            )
        }

        @Bean
        fun botUserInitializer(
            appserviceUserService: AppserviceUserService,
            botProperties: MatrixBotProperties
        ): BotUserInitializer {
            return BotUserInitializer(appserviceUserService, botProperties)
        }

        @Bean
        fun matrixEventTransactionService(eventTransactionRepository: MatrixEventTransactionRepository): MatrixEventTransactionService {
            return MatrixEventTransactionService(eventTransactionRepository)
        }

        @Bean
        @ConditionalOnMissingBean
        fun defaultAppserviceEventService(
            eventHandler: List<MatrixEventHandler<*>>,
            eventTransactionService: MatrixEventTransactionService
        ): AppserviceEventTnxService {
            return DefaultAppserviceEventTnxService(eventTransactionService)
        }

        @Bean
        @ConditionalOnMissingBean
        fun defaultAppserviceRoomService(
            roomService: MatrixRoomService,
            helper: BotServiceHelper,
            matrixClient: MatrixClient
        ): AppserviceRoomService {
            return DefaultAppserviceRoomService(roomService, helper, matrixClient)
        }

        @Bean
        @ConditionalOnMissingBean
        fun defaultAppserviceUserService(
            matrixUserService: MatrixUserService,
            helper: BotServiceHelper,
            botProperties: MatrixBotProperties,
            matrixClient: MatrixClient
        ): AppserviceUserService {
            return DefaultAppserviceUserService(matrixUserService, helper, botProperties, matrixClient)
        }

        @Bean
        fun appserviceMemberEventHandler(
            membershipChangeHandler: MembershipChangeHandler,
            appserviceUserService: AppserviceUserService
        ): AppserviceMemberEventHandler {
            return AppserviceMemberEventHandler(membershipChangeHandler, appserviceUserService)
        }
    }


    @Bean
    fun appserviceBotServiceHelper(
        botProperties: MatrixBotProperties,
        appserviceProperties: MatrixAppserviceConfigurationProperties
    ): BotServiceHelper {
        return BotServiceHelper(
            botProperties,
            appserviceProperties.namespaces.users.map { Regex(it.localpartRegex) }.toSet(),
            appserviceProperties.namespaces.rooms.map { Regex(it.localpartRegex) }.toSet()
        )
    }
}