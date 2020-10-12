package net.folivo.matrix.bot.config

import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.appservice.api.event.AppserviceEventService
import net.folivo.matrix.appservice.api.room.AppserviceRoomService
import net.folivo.matrix.appservice.api.user.AppserviceUserService
import net.folivo.matrix.bot.appservice.event.DefaultAppserviceEventService
import net.folivo.matrix.bot.appservice.event.MatrixEventTransactionRepository
import net.folivo.matrix.bot.appservice.event.MatrixEventTransactionService
import net.folivo.matrix.bot.appservice.membership.AppserviceMemberEventHandler
import net.folivo.matrix.bot.appservice.membership.AppserviceMembershipChangeService
import net.folivo.matrix.bot.appservice.membership.MatrixMembershipRepository
import net.folivo.matrix.bot.appservice.membership.MatrixMembershipService
import net.folivo.matrix.bot.appservice.room.DefaultAppserviceRoomService
import net.folivo.matrix.bot.appservice.room.MatrixRoomAliasRepository
import net.folivo.matrix.bot.appservice.room.MatrixRoomRepository
import net.folivo.matrix.bot.appservice.room.MatrixRoomService
import net.folivo.matrix.bot.appservice.sync.InitialSyncService
import net.folivo.matrix.bot.appservice.sync.MatrixSyncService
import net.folivo.matrix.bot.appservice.user.BotUserInitializer
import net.folivo.matrix.bot.appservice.user.DefaultAppserviceUserService
import net.folivo.matrix.bot.appservice.user.MatrixUserRepository
import net.folivo.matrix.bot.appservice.user.MatrixUserService
import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.bot.membership.MembershipChangeHandler
import net.folivo.matrix.bot.membership.MembershipChangeService
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.restclient.MatrixClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories


@Configuration
@ConditionalOnProperty(prefix = "matrix.bot", name = ["mode"], havingValue = "APPSERVICE")
@EnableR2dbcRepositories(basePackages = ["net.folivo.matrix.bot.appservice"])
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
    @ConditionalOnMissingBean
    fun defaultAppserviceMembershipChangeService(
            roomService: MatrixRoomService,
            membershipService: MatrixMembershipService,
            userService: MatrixUserService,
            matrixClient: MatrixClient,
            helper: BotServiceHelper
    ): MembershipChangeService {
        return AppserviceMembershipChangeService(
                roomService,
                membershipService,
                userService,
                matrixClient,
                helper
        )
    }

    @Bean
    fun matrixMembershipService(membershipRepository: MatrixMembershipRepository): MatrixMembershipService {
        return MatrixMembershipService(membershipRepository)
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
    fun matrixRoomService(
            roomRepository: MatrixRoomRepository,
            roomAliasRepository: MatrixRoomAliasRepository,
            matrixClient: MatrixClient,
            membershipService: MatrixMembershipService
    ): MatrixRoomService {
        return MatrixRoomService(roomRepository, roomAliasRepository, matrixClient, membershipService)
    }

    @Bean
    @Profile("initialsync")
    fun initialSyncService(
            userService: MatrixUserService,
            roomService: MatrixRoomService,
            syncService: MatrixSyncService
    ): InitialSyncService {
        return InitialSyncService(userService, roomService, syncService)
    }

    @Bean
    fun matrixSyncService(
            roomService: MatrixRoomService,
            helper: BotServiceHelper,
            matrixClient: MatrixClient,
            membershipChangeService: MembershipChangeService
    ): MatrixSyncService {
        return MatrixSyncService(roomService, helper, matrixClient, membershipChangeService)
    }

    @Bean
    @Profile("!initialsync")
    fun botUserInitializer(
            appserviceHandlerHelper: AppserviceHandlerHelper,
            botServiceHelper: BotServiceHelper
    ): BotUserInitializer {
        return BotUserInitializer(appserviceHandlerHelper, botServiceHelper)
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
    fun matrixUserService(
            userRepository: MatrixUserRepository,
            helper: BotServiceHelper
    ): MatrixUserService {
        return MatrixUserService(userRepository, helper)
    }

}