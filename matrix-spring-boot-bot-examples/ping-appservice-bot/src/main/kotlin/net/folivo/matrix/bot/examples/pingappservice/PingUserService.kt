package net.folivo.matrix.bot.examples.pingappservice

import net.folivo.matrix.appservice.api.user.CreateUserParameter
import net.folivo.matrix.bot.appservice.DefaultMatrixAppserviceUserService
import net.folivo.matrix.bot.appservice.MatrixAppserviceServiceHelper
import net.folivo.matrix.bot.config.MatrixBotProperties
import org.springframework.stereotype.Service

@Service
class PingUserService(
        private val botProperties: MatrixBotProperties,
        helper: MatrixAppserviceServiceHelper
) : DefaultMatrixAppserviceUserService(helper) {
    override suspend fun getCreateUserParameter(userId: String): CreateUserParameter {
        return if (userId == "@${botProperties.username}:${botProperties.serverName}") {
            CreateUserParameter("PING BOT 2000")
        } else {
            val username = userId.removePrefix("@ping_").substringBefore(":")
            val displayName = "$username (PING)"
            CreateUserParameter(displayName)
        }
    }
}