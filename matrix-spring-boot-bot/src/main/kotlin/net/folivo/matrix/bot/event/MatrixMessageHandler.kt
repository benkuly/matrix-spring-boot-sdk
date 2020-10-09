package net.folivo.matrix.bot.event

import net.folivo.matrix.core.model.events.m.room.message.MessageEvent

interface MatrixMessageHandler {

    suspend fun handleMessage(content: MessageEvent.MessageEventContent, context: MessageContext)

}