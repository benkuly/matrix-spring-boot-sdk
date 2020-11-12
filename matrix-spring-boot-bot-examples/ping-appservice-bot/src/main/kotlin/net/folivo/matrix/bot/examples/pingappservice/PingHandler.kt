package net.folivo.matrix.bot.examples.pingappservice

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import net.folivo.matrix.bot.event.MatrixMessageHandler
import net.folivo.matrix.bot.event.MessageContext
import net.folivo.matrix.bot.user.MatrixUserService
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import net.folivo.matrix.restclient.MatrixClient
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

    override suspend fun handleMessage(content: MessageEvent.MessageEventContent, context: MessageContext) {
        if (content is TextMessageEventContent) {
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