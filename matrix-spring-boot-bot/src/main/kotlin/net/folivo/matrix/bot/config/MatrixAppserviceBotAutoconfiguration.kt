package net.folivo.matrix.bot.config

import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.appservice.api.event.AppserviceEventService
import net.folivo.matrix.appservice.api.room.AppserviceRoomService
import net.folivo.matrix.appservice.api.user.AppserviceUserService
import net.folivo.matrix.appservice.config.AppserviceProperties
import net.folivo.matrix.bot.appservice.AppserviceMemberEventHandler
import net.folivo.matrix.bot.appservice.BotUserInitializer
import net.folivo.matrix.bot.appservice.DefaultAppserviceRoomService
import net.folivo.matrix.bot.appservice.DefaultAppserviceUserService
import net.folivo.matrix.bot.appservice.event.DefaultAppserviceEventService
import net.folivo.matrix.bot.appservice.event.MatrixEventTransactionRepository
import net.folivo.matrix.bot.appservice.event.MatrixEventTransactionService
import net.folivo.matrix.bot.appservice.sync.InitialSyncService
import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.bot.membership.MatrixMembershipSyncService
import net.folivo.matrix.bot.membership.MembershipChangeHandler
import net.folivo.matrix.bot.room.MatrixRoomService
import net.folivo.matrix.bot.user.MatrixUserService
import net.folivo.matrix.bot.util.BotServiceHelper
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@ConditionalOnProperty(prefix = "matrix.bot", name = ["mode"], havingValue = "APPSERVICE")
class MatrixAppserviceBotAutoconfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun defaultAppserviceEventService(
            eventHandler: List<MatrixEventHandler>,
            eventTransactionService: MatrixEventTransactionService
    ): AppserviceEventService {
        return DefaultAppserviceEventService(eventTransactionService, eventHandler)
    }

    @Bean
    fun matrixEventTransactionService(eventTransactionRepository: MatrixEventTransactionRepository): MatrixEventTransactionService {
        return MatrixEventTransactionService(eventTransactionRepository)
    }

    @Bean
    fun appserviceMemberEventHandler(
            membershipChangeHandler: MembershipChangeHandler,
            appserviceHelper: AppserviceHandlerHelper
    ): AppserviceMemberEventHandler {
        return AppserviceMemberEventHandler(membershipChangeHandler, appserviceHelper)
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultAppserviceRoomService(roomService: MatrixRoomService, helper: BotServiceHelper): AppserviceRoomService {
        return DefaultAppserviceRoomService(roomService, helper)
    }

    @Bean
    @Profile("initialsync")
    fun initialSyncService(
            userService: MatrixUserService,
            roomService: MatrixRoomService,
            membershipSyncService: MatrixMembershipSyncService
    ): InitialSyncService {
        return InitialSyncService(userService, roomService, membershipSyncService)
    }

    @Bean
    @Profile("!initialsync")
    fun botUserInitializer(
            appserviceHandlerHelper: AppserviceHandlerHelper,
            botProperties: MatrixBotProperties
    ): BotUserInitializer {
        return BotUserInitializer(appserviceHandlerHelper, botProperties)
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultAppserviceUserService(
            matrixUserService: MatrixUserService,
            helper: BotServiceHelper,
            botProperties: MatrixBotProperties
    ): AppserviceUserService {
        return DefaultAppserviceUserService(matrixUserService, helper, botProperties)
    }

    @Bean
    fun botServiceHelper(
            botProperties: MatrixBotProperties,
            appserviceProperties: AppserviceProperties
    ): BotServiceHelper {
        return BotServiceHelper(
                botProperties,
                appserviceProperties.namespaces.users.map { Regex(it.regex) }.toSet(),
                appserviceProperties.namespaces.rooms.map { Regex(it.regex) }.toSet()
        )
    }
}