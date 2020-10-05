package net.folivo.matrix.bot.examples.pingappservice

import net.folivo.matrix.bot.handler.MatrixMessageHandler
import net.folivo.matrix.bot.handler.MessageContext
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.NoticeMessageEventContent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PingHandler : MatrixMessageHandler {
    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun handleMessage(content: MessageEvent.MessageEventContent, context: MessageContext) {
        if (content is TextMessageEventContent) {
            if (content.body.contains("ping")) {
                val messageId = context.answer(NoticeMessageEventContent("pong"))
                LOG.info("pong (messageId: $messageId)")
            }
        }
    }
}