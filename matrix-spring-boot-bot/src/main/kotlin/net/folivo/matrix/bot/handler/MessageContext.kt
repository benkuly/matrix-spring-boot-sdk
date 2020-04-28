package net.folivo.matrix.bot.handler

import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.restclient.MatrixClient
import reactor.core.publisher.Mono

class MessageContext(
        val matrixClient: MatrixClient,
        val originalEvent: MessageEvent<*>,
        val roomId: String
) {

    fun answer(
            content: MessageEvent.MessageEventContent
    ): Mono<String> {
        return matrixClient.roomsApi.sendRoomEvent(
                roomId = roomId,
                eventContent = content
        )
    }

}