package net.folivo.matrix.bot.config

import net.folivo.matrix.bot.event.*
import net.folivo.matrix.bot.membership.*
import net.folivo.matrix.bot.room.MatrixRoomAliasRepository
import net.folivo.matrix.bot.room.MatrixRoomRepository
import net.folivo.matrix.bot.room.MatrixRoomService
import net.folivo.matrix.bot.user.MatrixUserRepository
import net.folivo.matrix.bot.user.MatrixUserService
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.sync.SyncBatchTokenService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
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
    @ConditionalOnMissingBean
    fun persistentSyncBatchTokenService(
            syncBatchTokenRepository: MatrixSyncBatchTokenRepository,
            userService: MatrixUserService,
            helper: BotServiceHelper
    ): SyncBatchTokenService {
        return PersistentSyncBatchTokenService(syncBatchTokenRepository, userService, helper)
    }

    @Bean
    fun matrixUserService(
            userRepository: MatrixUserRepository,
            helper: BotServiceHelper
    ): MatrixUserService {
        return MatrixUserService(userRepository, helper)
    }

    @Bean
    fun matrixRoomService(
            roomRepository: MatrixRoomRepository,
            roomAliasRepository: MatrixRoomAliasRepository
    ): MatrixRoomService {
        return MatrixRoomService(roomRepository, roomAliasRepository)
    }

    @Bean
    fun matrixMembershipService(
            membershipRepository: MatrixMembershipRepository,
            userService: MatrixUserService,
            roomService: MatrixRoomService
    ): MatrixMembershipService {
        return MatrixMembershipService(membershipRepository, userService, roomService)
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultMembershipChangeService(
            roomService: MatrixRoomService,
            membershipService: MatrixMembershipService,
            userService: MatrixUserService,
            matrixClient: MatrixClient,
            helper: BotServiceHelper
    ): DefaultMembershipChangeService {
        return DefaultMembershipChangeService(
                roomService,
                membershipService,
                userService,
                matrixClient,
                helper
        )
    }

    @Bean
    fun membershipChangeHandler(
            matrixClient: MatrixClient,
            membershipChangeService: MembershipChangeService,
            botHelper: BotServiceHelper,
            botProperties: MatrixBotProperties
    ): MembershipChangeHandler {
        return MembershipChangeHandler(matrixClient, membershipChangeService, botHelper, botProperties)
    }
}