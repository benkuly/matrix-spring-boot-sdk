package net.folivo.matrix.bot.examples.pingappservice

import net.folivo.matrix.bot.appservice.MatrixAppserviceServiceHelper
import net.folivo.matrix.bot.handler.MatrixMessageContentHandler
import net.folivo.matrix.bot.handler.MessageContext
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.NoticeMessageEventContent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PingHandler(
        private val matrixClient: MatrixClient,
        private val helper: MatrixAppserviceServiceHelper
) : MatrixMessageContentHandler {
    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun handleMessage(content: MessageEvent.MessageEventContent, context: MessageContext) {
        if (content is TextMessageEventContent) {
            if (content.body.contains("ping")) {
                matrixClient.roomsApi.getJoinedMembers(context.roomId)
                        .joined.keys
                        .filter { helper.isManagedUser(it) }
                        .forEach { member ->
                            val messageId = context.answer(NoticeMessageEventContent("pong"), asUserId = member)
                            LOG.info("pong (messageid: $messageId)")
                        }
            }
        }
    }
}