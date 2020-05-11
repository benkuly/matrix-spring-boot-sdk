package net.folivo.matrix.core.model.events.m.room

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.annotation.MatrixEvent
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.StateEvent
import net.folivo.matrix.core.model.events.StateEventContent
import net.folivo.matrix.core.model.events.StrippedStateEvent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-room-member">matrix spec</a>
 */
@MatrixEvent("m.room.member")
class MemberEvent : StateEvent<MemberEvent.MemberEventContent, MemberEvent.MemberUnsignedData> {

    constructor(
            content: MemberEventContent,
            id: String,
            sender: String,
            originTimestamp: Long,
            roomId: String? = null,
            unsigned: MemberUnsignedData,
            stateKey: String,
            previousContent: MemberEventContent? = null
    ) : super(
            type = "m.room.member",
            content = content,
            id = id,
            sender = sender,
            originTimestamp = originTimestamp,
            roomId = roomId,
            unsigned = unsigned,
            stateKey = stateKey,
            previousContent = previousContent
    )

    class MemberUnsignedData : UnsignedData {
        constructor(
                inviteRoomState: List<StrippedStateEvent> = emptyList(),
                age: Long? = null,
                redactedBecause: Event<*>? = null,
                transactionId: String? = null
        ) : super(
                age = age,
                redactedBecause = redactedBecause,
                transactionId = transactionId
        ) {
            this.inviteRoomState = inviteRoomState
        }

        @JsonProperty("invite_room_state")
        val inviteRoomState: List<StrippedStateEvent>
    }

    data class MemberEventContent(
            @JsonProperty("avatar_url")
            val avatarUrl: String? = null,
            @JsonProperty("displayname")
            val displayName: String? = null,
            @JsonProperty("membership")
            val membership: Membership,
            @JsonProperty("is_direct")
            val isDirect: Boolean? = null,
            @JsonProperty("third_party_invite")
            val thirdPartyInvite: Invite? = null
    ) : StateEventContent {
        enum class Membership {
            @JsonProperty("invite")
            INVITE,

            @JsonProperty("join")
            JOIN,

            @JsonProperty("knock")
            KNOCK,

            @JsonProperty("leave")
            LEAVE,

            @JsonProperty("ban")
            BAN
        }

        data class Invite(
                @JsonProperty("display_name")
                val displayName: String,
                @JsonProperty("signed")
                val signed: Signed
        ) {
            data class Signed(
                    @JsonProperty("mxid")
                    val mxid: String,
                    @JsonProperty("signatures")
                    val signatures: Any, // TODO signatures
                    @JsonProperty("token")
                    val token: String
            )
        }
    }

}