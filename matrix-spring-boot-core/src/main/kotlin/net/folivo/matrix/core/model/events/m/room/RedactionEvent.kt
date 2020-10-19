package net.folivo.matrix.core.model.events.m.room

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.annotation.MatrixEvent
import net.folivo.matrix.core.model.MatrixId.*
import net.folivo.matrix.core.model.events.RoomEventContent
import net.folivo.matrix.core.model.events.StandardRoomEvent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-room-create">matrix spec</a>
 */
@MatrixEvent("m.room.redaction")
class RedactionEvent : StandardRoomEvent<RedactionEvent.RedactionEventContent> {

    constructor(
            content: RedactionEventContent,
            id: EventId,
            sender: UserId,
            originTimestamp: Long,
            roomId: RoomId? = null,
            redacts: EventId,
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
    val redacts: EventId

    data class RedactionEventContent(
            @JsonProperty("reason")
            val reason: String?
    ) : RoomEventContent
}