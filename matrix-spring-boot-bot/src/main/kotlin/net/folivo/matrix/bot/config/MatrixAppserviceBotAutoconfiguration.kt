package net.folivo.matrix.bot.config

import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.appservice.config.MatrixAppserviceProperties
import net.folivo.matrix.bot.appservice.*
import net.folivo.matrix.bot.handler.AutoJoinService
import net.folivo.matrix.bot.handler.MatrixEventHandler
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
    fun matrixAppserviceServiceHelper(appserviceProperties: MatrixAppserviceProperties): MatrixAppserviceServiceHelper {
        val asUserName = matrixBotProperties.username
                         ?: throw MissingRequiredPropertyException("matrix.bot.username")

        return MatrixAppserviceServiceHelper(
                usersRegex = appserviceProperties.namespaces.users.map { it.regex },
                roomsRegex = appserviceProperties.namespaces.rooms.map { it.regex },
                asUsername = asUserName
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultMatrixAppserviceRoomService(helper: MatrixAppserviceServiceHelper): MatrixAppserviceRoomService {
        return DefaultMatrixAppserviceRoomService(helper)
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultMatrixAppserviceUserService(helper: MatrixAppserviceServiceHelper): MatrixAppserviceUserService {
        return DefaultMatrixAppserviceUserService(helper)
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultMatrixAppserviceEventService(eventHandler: List<MatrixEventHandler>): MatrixAppserviceEventService {
        return DefaultMatrixAppserviceEventService(eventHandler)
    }

    @Bean
    @ConditionalOnMissingBean
    fun membershipEventHandler(
            autoJoinService: AutoJoinService,
            matrixClient: MatrixClient,
            matrixAppserviceRoomService: MatrixAppserviceRoomService,
            appserviceProperties: MatrixAppserviceProperties,
            appserviceHandlerHelper: AppserviceHandlerHelper
    ): MembershipEventHandler {
        val asUserName = matrixBotProperties.username
                         ?: throw MissingRequiredPropertyException("matrix.bot.username")

        return MembershipEventHandler(
                autoJoinService = autoJoinService,
                matrixClient = matrixClient,
                roomService = matrixAppserviceRoomService,
                asUsername = asUserName,
                usersRegex = appserviceProperties.namespaces.users.map { it.regex },
                serverName = matrixBotProperties.serverName,
                autoJoin = matrixBotProperties.autoJoin,
                helper = appserviceHandlerHelper,
                trackMembershipMode = matrixBotProperties.trackMembership

        )
    }
}