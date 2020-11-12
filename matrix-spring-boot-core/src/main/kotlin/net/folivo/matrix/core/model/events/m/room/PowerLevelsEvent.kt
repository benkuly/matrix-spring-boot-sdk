package net.folivo.matrix.core.model.events.m.room

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.annotation.MatrixEvent
import net.folivo.matrix.core.model.MatrixId.*
import net.folivo.matrix.core.model.events.StandardStateEvent
import net.folivo.matrix.core.model.events.StateEventContent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-room-power-levels">matrix spec</a>
 */
@MatrixEvent("m.room.power_levels")
class PowerLevelsEvent : StandardStateEvent<PowerLevelsEvent.PowerLevelsEventContent> {

    constructor(
            content: PowerLevelsEventContent,
            id: EventId,
            sender: UserId,
            originTimestamp: Long,
            roomId: RoomId? = null,
            unsigned: UnsignedData,
            previousContent: PowerLevelsEventContent? = null
    ) : super(
            type = "m.room.power_levels",
            content = content,
            id = id,
            sender = sender,
            originTimestamp = originTimestamp,
            roomId = roomId,
            unsigned = unsigned,
            stateKey = "",
            previousContent = previousContent
    )

    data class PowerLevelsEventContent(
            @JsonProperty("ban")
            val ban: Int = 50,
            @JsonProperty("events")
            val events: Map<String, Int> = emptyMap(),
            @JsonProperty("events_default")
            val eventsDefault: Int = 0,
            @JsonProperty("invite")
            val invite: Int = 50,
            @JsonProperty("kick")
            val kick: Int = 50,
            @JsonProperty("redact")
            val redact: Int = 50,
            @JsonProperty("state_default")
            val stateDefault: Int = 50,
            @JsonProperty("users")
            val users: Map<UserId, Int> = emptyMap(),
            @JsonProperty("users_default")
            val usersDefault: Int = 0,
            @JsonProperty("notifications")
            val notifications: Notifications? = null
    ) : StateEventContent {
        data class Notifications(
                @JsonProperty("room")
                val room: Int = 50
        )
    }
}