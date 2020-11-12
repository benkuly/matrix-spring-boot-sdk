package net.folivo.matrix.bot.appservice

import kotlinx.coroutines.runBlocking
import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.bot.config.MatrixBotProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

class BotUserInitializer(
        private val appserviceHandlerHelper: AppserviceHandlerHelper,
        private val botProperties: MatrixBotProperties
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    @EventListener(ApplicationReadyEvent::class)
    fun initializeBotUser() {
        runBlocking {
            LOG.info("Initializing appservice bot")

            val userId = botProperties.botUserId

            try {
                appserviceHandlerHelper.registerManagedUser(userId)
            } catch (error: Throwable) {
                LOG.warn("failed to initialize appservice bot: ${error.message}")
            }
        }
    }
}