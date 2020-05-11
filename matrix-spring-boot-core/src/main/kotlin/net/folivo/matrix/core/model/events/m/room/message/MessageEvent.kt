package net.folivo.matrix.core.model.events.m.room.message

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.annotation.MatrixEvent
import net.folivo.matrix.core.model.events.RoomEventContent
import net.folivo.matrix.core.model.events.StandardRoomEvent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-room-message">matrix spec</a>
 */
@MatrixEvent("m.room.message")
class MessageEvent<C : MessageEvent.MessageEventContent> : StandardRoomEvent<MessageEvent.MessageEventContent> {

    constructor(
            content: C,
            id: String,
            sender: String,
            originTimestamp: Long,
            roomId: String? = null,
            unsigned: UnsignedData
    ) : super(
            type = "m.room.message",
            content = content,
            id = id,
            sender = sender,
            originTimestamp = originTimestamp,
            roomId = roomId,
            unsigned = unsigned
    )

    abstract class MessageEventContent : RoomEventContent {

        constructor(body: String, messageType: String) {
            this.body = body
            this.messageType = messageType
        }

        @JsonProperty("body")
        val body: String

        @JsonProperty("msgtype")
        val messageType: String
    }
}