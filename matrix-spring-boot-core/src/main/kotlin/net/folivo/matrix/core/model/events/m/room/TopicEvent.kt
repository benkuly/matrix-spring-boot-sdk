package net.folivo.matrix.core.model.events.m.room

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.annotation.MatrixEvent
import net.folivo.matrix.core.model.events.StandardStateEvent
import net.folivo.matrix.core.model.events.StateEventContent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-room-topic">matrix spec</a>
 */
@MatrixEvent("m.room.topic")
class TopicEvent : StandardStateEvent<TopicEvent.TopicEventContent> {

    constructor(
            content: TopicEventContent,
            id: String,
            sender: String,
            originTimestamp: Long,
            roomId: String? = null,
            unsigned: UnsignedData,
            previousContent: TopicEventContent? = null
    ) : super(
            type = "m.room.topic",
            content = content,
            id = id,
            sender = sender,
            originTimestamp = originTimestamp,
            roomId = roomId,
            unsigned = unsigned,
            stateKey = "",
            previousContent = previousContent
    )

    data class TopicEventContent(
            @JsonProperty("topic")
            val topic: String
    ) : StateEventContent
}