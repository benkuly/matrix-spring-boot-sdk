package net.folivo.matrix.bot.appservice

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.bot.config.MatrixBotProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

class BotUserInitializer(
        private val botProperties: MatrixBotProperties,
        private val appserviceHandlerHelper: AppserviceHandlerHelper
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

        appserviceHandlerHelper.registerManagedUser(userId)
    }
}