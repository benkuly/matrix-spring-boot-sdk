package net.folivo.matrix.core.model.events.m.room

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.annotation.MatrixEvent
import net.folivo.matrix.core.model.events.StandardStateEvent
import net.folivo.matrix.core.model.events.StateEventContent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-room-pinned-events">matrix spec</a>
 */
@MatrixEvent("m.room.pinned_events")
class PinnedEventsEvent : StandardStateEvent<PinnedEventsEvent.PinnedEventsEventContent> {

    constructor(
            content: PinnedEventsEventContent,
            id: String,
            sender: String,
            originTimestamp: Long,
            roomId: String? = null,
            unsigned: UnsignedData,
            previousContent: PinnedEventsEventContent? = null
    ) : super(
            type = "m.room.pinned_events",
            content = content,
            id = id,
            sender = sender,
            originTimestamp = originTimestamp,
            roomId = roomId,
            unsigned = unsigned,
            stateKey = "",
            previousContent = previousContent
    )

    data class PinnedEventsEventContent(
            @JsonProperty("pinned")
            val pinned: List<String>
    ) : StateEventContent
}