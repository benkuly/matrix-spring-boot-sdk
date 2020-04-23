package net.folivo.matrix.restclient.model.events.m.room

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.restclient.annotation.MatrixEvent
import net.folivo.matrix.restclient.model.events.StandardStateEvent
import net.folivo.matrix.restclient.model.events.StateEventContent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-room-aliases">matrix spec</a>
 */
@MatrixEvent("m.room.aliases")
class AliasesEvent : StandardStateEvent<AliasesEvent.AliasesEventContent> {

    constructor(
            content: AliasesEventContent,
            sender: String,
            stateKey: String,
            id: String,
            originTimestamp: Long,
            unsigned: UnsignedData,
            roomId: String? = null,
            previousContent: AliasesEventContent? = null
    ) : super(
            type = "m.room.aliases",
            content = content,
            id = id,
            sender = sender,
            originTimestamp = originTimestamp,
            roomId = roomId,
            unsigned = unsigned,
            stateKey = stateKey,
            previousContent = previousContent
    )

    data class AliasesEventContent(
            @JsonProperty("aliases")
            val aliases: List<String> = listOf()
    ) : StateEventContent
}