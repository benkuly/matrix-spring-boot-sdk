package net.folivo.matrix.core.model.events.m.room

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.annotation.MatrixEvent
import net.folivo.matrix.core.model.events.StandardStateEvent
import net.folivo.matrix.core.model.events.StateEventContent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-room-create">matrix spec</a>
 */
@MatrixEvent("m.room.create")
class CreateEvent : StandardStateEvent<CreateEvent.CreateEventContent> {

    constructor(
            content: CreateEventContent,
            id: String,
            sender: String,
            originTimestamp: Long,
            roomId: String? = null,
            unsigned: UnsignedData,
            previousContent: CreateEventContent? = null
    ) : super(
            type = "m.room.create",
            content = content,
            id = id,
            sender = sender,
            originTimestamp = originTimestamp,
            roomId = roomId,
            unsigned = unsigned,
            stateKey = "",
            previousContent = previousContent
    )

    data class CreateEventContent(
            @JsonProperty("creator")
            val creator: String,
            @JsonProperty("m.federate")
            val federate: Boolean = true,
            @JsonProperty("room_version")
            val roomVersion: String = "1",
            @JsonProperty("predecessor")
            val predecessor: PreviousRoom? = null
    ) : StateEventContent {
        data class PreviousRoom(
                @JsonProperty("room_id")
                val roomId: String,
                @JsonProperty("event_id")
                val eventId: String
        )
    }
}