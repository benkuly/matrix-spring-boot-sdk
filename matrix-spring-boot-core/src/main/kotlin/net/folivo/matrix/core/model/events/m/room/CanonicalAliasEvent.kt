package net.folivo.matrix.core.model.events.m.room

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.annotation.MatrixEvent
import net.folivo.matrix.core.model.events.StandardStateEvent
import net.folivo.matrix.core.model.events.StateEventContent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-room-canonical-alias">matrix spec</a>
 */
@MatrixEvent("m.room.canonical_alias")
class CanonicalAliasEvent : StandardStateEvent<CanonicalAliasEvent.CanonicalAliasEventContent> {

    constructor(
            content: CanonicalAliasEventContent,
            id: String,
            sender: String,
            originTimestamp: Long,
            roomId: String? = null,
            unsigned: UnsignedData,
            previousContent: CanonicalAliasEventContent? = null
    ) : super(
            type = "m.room.canonical_alias",
            content = content,
            id = id,
            sender = sender,
            originTimestamp = originTimestamp,
            roomId = roomId,
            unsigned = unsigned,
            stateKey = "",
            previousContent = previousContent
    )

    data class CanonicalAliasEventContent(
            @JsonProperty("alias")
            val alias: String
    ) : StateEventContent
}