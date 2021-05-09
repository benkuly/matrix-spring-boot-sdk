package net.folivo.spring.matrix.bot.config

import net.folivo.spring.matrix.bot.event.MatrixMessageHandler
import net.folivo.spring.matrix.bot.event.MatrixSyncBatchTokenRepository
import net.folivo.spring.matrix.bot.event.MessageEventHandler
import net.folivo.spring.matrix.bot.event.PersistentSyncBatchTokenService
import net.folivo.spring.matrix.bot.membership.*
import net.folivo.spring.matrix.bot.room.MatrixRoomAliasRepository
import net.folivo.spring.matrix.bot.room.MatrixRoomRepository
import net.folivo.spring.matrix.bot.room.MatrixRoomService
import net.folivo.spring.matrix.bot.user.MatrixUserRepository
import net.folivo.spring.matrix.bot.user.MatrixUserService
import net.folivo.spring.matrix.bot.util.BotServiceHelper
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.client.rest.api.sync.SyncBatchTokenService
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
    ): MessageEventHandler {
        return MessageEventHandler(matrixMessageHandler, matrixClient)
    }

    @Bean
    @ConditionalOnMissingBean
    fun persistentSyncBatchTokenService(
        syncBatchTokenRepository: MatrixSyncBatchTokenRepository,
        userService: MatrixUserService,
        botProperties: MatrixBotProperties
    ): SyncBatchTokenService {
        return PersistentSyncBatchTokenService(syncBatchTokenRepository, userService, botProperties)
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
        membershipSyncService: MatrixMembershipSyncService,
        matrixClient: MatrixClient,
        botProperties: MatrixBotProperties
    ): DefaultMembershipChangeService {
        return DefaultMembershipChangeService(
            roomService,
            membershipService,
            userService,
            membershipSyncService,
            matrixClient,
            botProperties
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

    @Bean
    fun matrixMembershipSyncService(
        matrixClient: MatrixClient,
        roomService: MatrixRoomService,
        membershipService: MatrixMembershipService,
        helper: BotServiceHelper,
        botProperties: MatrixBotProperties
    ): MatrixMembershipSyncService {
        return MatrixMembershipSyncService(matrixClient, roomService, membershipService, helper, botProperties)
    }
}