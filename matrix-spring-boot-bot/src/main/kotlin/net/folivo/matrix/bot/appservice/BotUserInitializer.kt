package net.folivo.matrix.bot.appservice

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

class BotUserInitializer(
        private val matrixClient: MatrixClient,
        private val botProperties: MatrixBotProperties,
        private val userService: MatrixAppserviceUserService
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    @EventListener(ApplicationReadyEvent::class)
    fun initializeBotUser() {
        GlobalScope.launch {
            initializeBotUserAsync()
        }
    }

    suspend fun initializeBotUserAsync() {
        LOG.info("Initializing appservice bot")
        val userId = "@${botProperties.username}:${botProperties.serverName}"
        try {
            matrixClient.userApi.register(
                    authenticationType = "m.login.application_service",
                    username = userId.trimStart('@').substringBefore(":")
            )
        } catch (error: MatrixServerException) {
            if (error.errorResponse.errorCode == "M_USER_IN_USE") {
                LOG.debug("bot user $userId has already been created")
            } else throw error
        }
        val displayName = userService.getCreateUserParameter(userId).displayName
        if (displayName != null) {
            matrixClient.userApi.setDisplayName(
                    userId,
                    displayName
            )
        }
        userService.saveUser(userId)
    }
}