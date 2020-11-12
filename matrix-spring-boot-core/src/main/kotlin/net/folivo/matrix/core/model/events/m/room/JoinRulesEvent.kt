package net.folivo.matrix.core.model.events.m.room

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.annotation.MatrixEvent
import net.folivo.matrix.core.model.MatrixId.*
import net.folivo.matrix.core.model.events.StandardStateEvent
import net.folivo.matrix.core.model.events.StateEventContent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-room-join-rules">matrix spec</a>
 */
@MatrixEvent("m.room.join_rules")
class JoinRulesEvent : StandardStateEvent<JoinRulesEvent.JoinRulesEventContent> {

    constructor(
            content: JoinRulesEventContent,
            id: EventId,
            sender: UserId,
            originTimestamp: Long,
            roomId: RoomId? = null,
            unsigned: UnsignedData,
            previousContent: JoinRulesEventContent? = null
    ) : super(
            type = "m.room.join_rules",
            content = content,
            id = id,
            sender = sender,
            originTimestamp = originTimestamp,
            roomId = roomId,
            unsigned = unsigned,
            stateKey = "",
            previousContent = previousContent
    )

    data class JoinRulesEventContent(
            @JsonProperty("join_rule")
            val joinRule: JoinRule
    ) : StateEventContent {
        enum class JoinRule {
            @JsonProperty("public")
            PUBLIC,

            @JsonProperty("knock")
            KNOCK,

            @JsonProperty("invite")
            INVITE,

            @JsonProperty("private")
            PRIVATE
        }
    }
}