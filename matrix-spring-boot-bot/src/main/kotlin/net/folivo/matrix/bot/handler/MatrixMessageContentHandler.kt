package net.folivo.matrix.bot.handler

import net.folivo.matrix.core.model.events.m.room.message.MessageEvent

interface MatrixMessageContentHandler {

    fun handleMessage(content: MessageEvent.MessageEventContent, context: MessageContext)

}