package net.folivo.matrix.common.model.events.m.room

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.common.annotation.MatrixEvent
import net.folivo.matrix.common.model.events.RoomEventContent
import net.folivo.matrix.common.model.events.StandardRoomEvent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-room-create">matrix spec</a>
 */
@MatrixEvent("m.room.redaction")
class RedactionEvent : StandardRoomEvent<RedactionEvent.RedactionEventContent> {

    constructor(
            content: RedactionEventContent,
            id: String,
            sender: String,
            originTimestamp: Long,
            roomId: String? = null,
            redacts: String,
            unsigned: UnsignedData
    ) : super(
            type = "m.room.redaction",
            content = content,
            id = id,
            sender = sender,
            originTimestamp = originTimestamp,
            roomId = roomId,
            unsigned = unsigned
    ) {
        this.redacts = redacts
    }

    @JsonProperty("redacts")
    val redacts: String

    data class RedactionEventContent(
            @JsonProperty("reason")
            val reason: String
    ) : RoomEventContent
}