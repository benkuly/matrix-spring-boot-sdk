package net.folivo.spring.matrix.bot.examples.pingappservice

import net.folivo.spring.matrix.bot.event.MatrixMessageHandler
import net.folivo.spring.matrix.bot.event.MessageContext
import net.folivo.trixnity.core.model.events.m.room.MessageEventContent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PingHandler : MatrixMessageHandler {
    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun handleMessage(content: MessageEventContent, context: MessageContext) {
        if (content is MessageEventContent.TextMessageEventContent) {
            if (content.body.contains("ping")) {
                val messageId = context.answer("pong")
                LOG.info("pong (messageId: $messageId)")
            }
        }
    }
}