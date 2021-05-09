package net.folivo.spring.matrix.bot.examples.pingappservice

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import net.folivo.spring.matrix.bot.event.MatrixMessageHandler
import net.folivo.spring.matrix.bot.event.MessageContext
import net.folivo.spring.matrix.bot.user.MatrixUserService
import net.folivo.spring.matrix.bot.util.BotServiceHelper
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.core.model.events.m.room.MessageEventContent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PingHandler(
    private val matrixClient: MatrixClient,
    private val helper: BotServiceHelper,
    private val userService: MatrixUserService
) : MatrixMessageHandler {
    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun handleMessage(content: MessageEventContent, context: MessageContext) {
        if (content is MessageEventContent.TextMessageEventContent) {
            if (content.body.contains("ping")) {
                userService.getUsersByRoom(context.roomId)
                    .filter { it.isManaged }
                    .collect { member ->
                        val messageId = context.answer("pong", asUserId = member.id)
                        LOG.info("pong (messageid: $messageId)")
                    }
            }
        }
    }
}