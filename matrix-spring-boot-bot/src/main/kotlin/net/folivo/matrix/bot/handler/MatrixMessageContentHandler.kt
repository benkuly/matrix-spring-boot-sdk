package net.folivo.matrix.bot.handler

import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import reactor.core.publisher.Mono

interface MatrixMessageContentHandler {

    fun handleMessage(content: MessageEvent.MessageEventContent, context: MessageContext): Mono<Void>

}