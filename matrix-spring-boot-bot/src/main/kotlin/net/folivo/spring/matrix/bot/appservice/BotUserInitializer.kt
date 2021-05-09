package net.folivo.spring.matrix.bot.appservice

import kotlinx.coroutines.runBlocking
import net.folivo.spring.matrix.bot.config.MatrixBotProperties
import net.folivo.trixnity.appservice.rest.user.AppserviceUserService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

class BotUserInitializer(
    private val appserviceUserService: AppserviceUserService,
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
                appserviceUserService.registerManagedUser(userId)
            } catch (error: Throwable) {
                LOG.warn("failed to initialize appservice bot: ${error.message}")
            }
        }
    }
}