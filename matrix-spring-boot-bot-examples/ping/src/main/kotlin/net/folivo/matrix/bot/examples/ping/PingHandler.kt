package net.folivo.matrix.bot.examples.ping

import net.folivo.matrix.bot.handler.MatrixMessageContentHandler
import net.folivo.matrix.bot.handler.MessageContext
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.NoticeMessageEventContent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PingHandler : MatrixMessageContentHandler {
    private val logger = LoggerFactory.getLogger(PingHandler::class.java)

    override fun handleMessage(content: MessageEvent.MessageEventContent, context: MessageContext) {
        if (content is TextMessageEventContent) {
            val body = content.body
            if (body.contains("ping")) {
                val id = context.answer(NoticeMessageEventContent("pong")).block()
                logger.info("pong (messageid: $id)")
            }
        }
    }
}