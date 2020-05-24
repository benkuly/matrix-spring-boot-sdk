package net.folivo.matrix.bot.examples.pingappservice

import net.folivo.matrix.bot.handler.MatrixMessageContentHandler
import net.folivo.matrix.bot.handler.MessageContext
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.NoticeMessageEventContent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class PingHandler(private val matrixClient: MatrixClient) : MatrixMessageContentHandler {
    private val logger = LoggerFactory.getLogger(PingHandler::class.java)

    override fun handleMessage(content: MessageEvent.MessageEventContent, context: MessageContext): Mono<Void> {
        if (content is TextMessageEventContent) {
            if (content.body.contains("ping")) {
                return matrixClient.roomsApi.getJoinedMembers(context.roomId)
                        .flatMapMany { Flux.fromIterable(it.joined.keys) }
                        .flatMap { member ->
                            context.answer(NoticeMessageEventContent("pong"), asUserId = member)
                                    .doOnSuccess { logger.info("pong (messageid: $it)") }
                        }.then()
            }
        }
        return Mono.empty()
    }
}