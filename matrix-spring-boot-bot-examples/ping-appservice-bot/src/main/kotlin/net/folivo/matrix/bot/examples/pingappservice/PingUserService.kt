package net.folivo.matrix.bot.examples.pingappservice

import net.folivo.matrix.appservice.api.user.RegisterUserParameter
import net.folivo.matrix.bot.appservice.DefaultAppserviceUserService
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.handler.BotServiceHelper
import org.springframework.stereotype.Service

@Service
class PingUserService(
        private val botProperties: MatrixBotProperties,
        helper: BotServiceHelper
) : DefaultAppserviceUserService(helper) {
    override suspend fun getRegisterUserParameter(userId: String): RegisterUserParameter {
        return if (userId == "@${botProperties.username}:${botProperties.serverName}") {
            RegisterUserParameter("PING BOT 2000")
        } else {
            val username = userId.removePrefix("@ping_").substringBefore(":")
            val displayName = "$username (PING)"
            RegisterUserParameter(displayName)
        }
    }
}