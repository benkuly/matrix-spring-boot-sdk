package net.folivo.spring.matrix.bot.config

import net.folivo.spring.matrix.bot.client.ClientMemberEventHandler
import net.folivo.spring.matrix.bot.client.MatrixClientSyncRunner
import net.folivo.spring.matrix.bot.event.EventHandlerRunner
import net.folivo.spring.matrix.bot.event.MatrixEventHandler
import net.folivo.spring.matrix.bot.membership.MembershipChangeHandler
import net.folivo.spring.matrix.bot.util.BotServiceHelper
import net.folivo.trixnity.client.rest.MatrixClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@ConditionalOnProperty(prefix = "matrix.bot", name = ["mode"], havingValue = "CLIENT", matchIfMissing = true)
class MatrixClientBotAutoconfiguration {

    @Bean
    fun clientMemberEventHandler(membershipChangeHandler: MembershipChangeHandler): ClientMemberEventHandler {
        return ClientMemberEventHandler(membershipChangeHandler)
    }

    @Bean
    fun matrixClientSyncRunner(
        matrixClient: MatrixClient,
    ): MatrixClientSyncRunner {
        return MatrixClientSyncRunner(
            matrixClient,
        )
    }

    @Bean
    fun eventHandlerRunner(
        matrixClient: MatrixClient,
        eventHandler: List<MatrixEventHandler<*>>,
    ): EventHandlerRunner {
        return EventHandlerRunner(
            matrixClient.sync,
            eventHandler
        )
    }

    @Bean
    fun clientBotServiceHelper(
        botProperties: MatrixBotProperties
    ): BotServiceHelper {
        return BotServiceHelper(botProperties, setOf(), setOf())
    }
}