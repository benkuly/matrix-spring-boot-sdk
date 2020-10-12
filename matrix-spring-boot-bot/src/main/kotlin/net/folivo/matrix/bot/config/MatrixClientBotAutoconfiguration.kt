package net.folivo.matrix.bot.config

import net.folivo.matrix.bot.client.ClientMemberEventHandler
import net.folivo.matrix.bot.client.ClientMembershipChangeService
import net.folivo.matrix.bot.client.MatrixClientBot
import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.bot.membership.MembershipChangeHandler
import net.folivo.matrix.bot.membership.MembershipChangeService
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.restclient.MatrixClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
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
    fun matrixClientBot(
            matrixClient: MatrixClient,
            eventHandler: List<MatrixEventHandler>,
            membershipChangeHandler: MembershipChangeHandler,
            helper: BotServiceHelper
    ): MatrixClientBot {
        return MatrixClientBot(
                matrixClient,
                eventHandler,
                membershipChangeHandler,
                helper
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultClientMembershipChangeService(): MembershipChangeService {
        return ClientMembershipChangeService()
    }

}