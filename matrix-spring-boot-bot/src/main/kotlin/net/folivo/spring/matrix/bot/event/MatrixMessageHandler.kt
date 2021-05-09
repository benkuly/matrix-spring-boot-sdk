package net.folivo.spring.matrix.bot.event

import net.folivo.trixnity.core.model.events.m.room.MessageEventContent

interface MatrixMessageHandler {

    suspend fun handleMessage(content: MessageEventContent, context: MessageContext)

}