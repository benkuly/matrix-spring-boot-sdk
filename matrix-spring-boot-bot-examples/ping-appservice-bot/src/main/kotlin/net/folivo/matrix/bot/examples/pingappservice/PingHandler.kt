package net.folivo.matrix.bot.examples.pingappservice

import net.folivo.matrix.bot.appservice.room.AppserviceRoomRepository
import net.folivo.matrix.bot.handler.MatrixMessageContentHandler
import net.folivo.matrix.bot.handler.MessageContext
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.NoticeMessageEventContent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class PingHandler(
        private val roomRepository: AppserviceRoomRepository
) : MatrixMessageContentHandler {
    private val logger = LoggerFactory.getLogger(PingHandler::class.java)

    override fun handleMessage(content: MessageEvent.MessageEventContent, context: MessageContext): Mono<Void> {
        if (content is TextMessageEventContent) {
            if (content.body.contains("ping")) {
                return roomRepository.findById(context.roomId)
                        .flatMapMany { Flux.fromIterable(it.members) }
                        .flatMap { member ->
                            context.answer(NoticeMessageEventContent("pong"), asUserId = member.userId)
                                    .doOnSuccess { logger.info("pong (messageid: $it)") }
                        }.then()
            }
        }
        return Mono.empty()
    }
}