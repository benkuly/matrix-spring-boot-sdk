package net.folivo.matrix.core.model.events.m.room

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.annotation.MatrixEvent
import net.folivo.matrix.core.model.MatrixId.*
import net.folivo.matrix.core.model.events.StandardStateEvent
import net.folivo.matrix.core.model.events.StateEventContent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-room-aliases">matrix spec</a>
 */
@MatrixEvent("m.room.canonical_alias")
class CanonicalAliasEvent : StandardStateEvent<CanonicalAliasEvent.CanonicalAliasEventContent> {

    constructor(
            content: CanonicalAliasEventContent,
            sender: UserId,
            id: EventId,
            originTimestamp: Long,
            unsigned: UnsignedData,
            roomId: RoomId? = null,
            previousContent: CanonicalAliasEventContent? = null
    ) : super(
            type = "m.room.aliases",
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
            val alias: RoomAliasId? = null,
            @JsonProperty("alt_aliases")
            val aliases: List<RoomAliasId> = listOf()
    ) : StateEventContent
}