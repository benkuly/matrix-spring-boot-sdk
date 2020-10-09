package net.folivo.matrix.bot.config

import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.appservice.api.event.AppserviceEventService
import net.folivo.matrix.appservice.api.room.AppserviceRoomService
import net.folivo.matrix.appservice.api.user.AppserviceUserService
import net.folivo.matrix.appservice.config.AppserviceProperties
import net.folivo.matrix.bot.appservice.BotUserInitializer
import net.folivo.matrix.bot.appservice.DefaultAppserviceEventService
import net.folivo.matrix.bot.appservice.DefaultAppserviceRoomService
import net.folivo.matrix.bot.appservice.DefaultAppserviceUserService
import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.bot.membership.AutoJoinCustomizer
import net.folivo.matrix.bot.membership.MembershipChangeHandler
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.restclient.MatrixClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@ConditionalOnProperty(prefix = "matrix.bot", name = ["mode"], havingValue = "APPSERVICE")
class MatrixAppserviceBotAutoconfiguration(private val matrixBotProperties: MatrixBotProperties) {

    @Bean
    @ConditionalOnMissingBean
    fun matrixAppserviceServiceHelper(appserviceProperties: AppserviceProperties): BotServiceHelper {
        val asUserName = matrixBotProperties.username
                         ?: throw MissingRequiredPropertyException("matrix.bot.username")

        return BotServiceHelper(
                usersRegex = appserviceProperties.namespaces.users.map { it.regex },
                roomsRegex = appserviceProperties.namespaces.rooms.map { it.regex },
                asUsername = asUserName
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultMatrixAppserviceRoomService(helper: BotServiceHelper): AppserviceRoomService {
        return DefaultAppserviceRoomService(helper)
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultMatrixAppserviceUserService(helper: BotServiceHelper): AppserviceUserService {
        return DefaultAppserviceUserService(helper)
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultMatrixAppserviceEventService(eventHandler: List<MatrixEventHandler>): AppserviceEventService {
        return DefaultAppserviceEventService(eventHandler)
    }

    @Bean
    @ConditionalOnMissingBean
    fun membershipEventHandler(
            autoJoinCustomizer: AutoJoinCustomizer,
            matrixClient: MatrixClient,
            appserviceRoomService: AppserviceRoomService,
            appserviceProperties: AppserviceProperties,
            appserviceHandlerHelper: AppserviceHandlerHelper
    ): MembershipChangeHandler {
        val asUserName = matrixBotProperties.username
                         ?: throw MissingRequiredPropertyException("matrix.bot.username")

        return MembershipChangeHandler(
                autoJoinService = autoJoinCustomizer,
                matrixClient = matrixClient,
                roomService = appserviceRoomService,
                asUsername = asUserName,
                usersRegex = appserviceProperties.namespaces.users.map { it.regex },
                serverName = matrixBotProperties.serverName,
                autoJoin = matrixBotProperties.autoJoin,
                helper = appserviceHandlerHelper,
                trackMembershipMode = matrixBotProperties.trackMembership

        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun botUserInitializer(matrixClient: MatrixClient, userService: AppserviceUserService): BotUserInitializer {
        return BotUserInitializer(
                matrixClient = matrixClient,
                botProperties = matrixBotProperties,
                userService = userService
        )
    }
}