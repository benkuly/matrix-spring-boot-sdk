package net.folivo.matrix.core.model.events.m.room

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.annotation.MatrixEvent
import net.folivo.matrix.core.model.MatrixId.*
import net.folivo.matrix.core.model.events.StandardStateEvent
import net.folivo.matrix.core.model.events.StateEventContent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-room-name">matrix spec</a>
 */
@MatrixEvent("m.room.name")
class NameEvent : StandardStateEvent<NameEvent.NameEventContent> {

    constructor(
            content: NameEventContent,
            id: EventId,
            sender: UserId,
            originTimestamp: Long,
            roomId: RoomId? = null,
            unsigned: UnsignedData,
            previousContent: NameEventContent? = null
    ) : super(
            type = "m.room.name",
            content = content,
            id = id,
            sender = sender,
            originTimestamp = originTimestamp,
            roomId = roomId,
            unsigned = unsigned,
            stateKey = "",
            previousContent = previousContent
    )

    data class NameEventContent(
            @JsonProperty("name")
            val name: String = ""
    ) : StateEventContent
}