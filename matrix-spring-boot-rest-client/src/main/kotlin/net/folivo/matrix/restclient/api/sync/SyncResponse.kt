package net.folivo.matrix.restclient.api.sync

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.common.model.events.Event
import net.folivo.matrix.common.model.events.StrippedStateEvent

data class SyncResponse(
        @JsonProperty("next_batch")
        val nextBatch: String,
        @JsonProperty("rooms")
        val room: Rooms,
        @JsonProperty("presence")
        val presence: Presence,
        @JsonProperty("account_data")
        val accountData: AccountData,
        @JsonProperty("to_device")
        val toDevice: ToDevice,
        @JsonProperty("device_lists")
        val deviceLists: DeviceLists,
        @JsonProperty("device_one_time_keys_count")
        val deviceOneTimeKeysCount: Map<String, Int>
) {
    data class Rooms(
            @JsonProperty("join")
            val join: Map<String, JoinedRoom>,
            @JsonProperty("invite")
            val invite: Map<String, InvitedRoom>,
            @JsonProperty("leave")
            val leave: Map<String, LeftRoom>
    ) {
        data class JoinedRoom(
                @JsonProperty("summary")
                val summary: RoomSummary,
                @JsonProperty("state")
                val state: State,
                @JsonProperty("timeline")
                val timeline: Timeline,
                @JsonProperty("ephemeral")
                val ephemeral: Ephemeral,
                @JsonProperty("account_data")
                val accountData: AccountData,
                @JsonProperty("unread_notifications")
                val unreadNotifications: UnreadNotificationCounts
        ) {
            data class RoomSummary(
                    @JsonProperty("m.heroes")
                    val heroes: List<String>? = null,
                    @JsonProperty("m.joined_member_count")
                    val joinedMemberCount: Int,
                    @JsonProperty("m.invited_member_count")
                    val invitedMemberCount: Int
            )

            data class Ephemeral(
                    @JsonProperty("events")
                    val events: List<Event<*>>
            )

            data class UnreadNotificationCounts(
                    @JsonProperty("highlight_count")
                    val highlightCount: Int,
                    @JsonProperty("notification_count")
                    val notificationCount: Int
            )
        }

        data class InvitedRoom(
                @JsonProperty("invite_state")
                val inviteState: InviteState
        ) {
            data class InviteState(
                    @JsonProperty("events")
                    val events: List<StrippedStateEvent>
            )
        }

        data class LeftRoom(
                @JsonProperty("state")
                val state: State,
                @JsonProperty("timeline")
                val timeline: Timeline,
                @JsonProperty("account_data")
                val accountData: AccountData
        )

        data class State(
                @JsonProperty("events")
                val events: List<Event<*>> //TODO should be StateEvent, but then we need custom logic of unknown events
        )

        data class Timeline(
                @JsonProperty("events")
                val events: List<Event<*>>,
                @JsonProperty("limited")
                val limited: Boolean,
                @JsonProperty("prev_batch")
                val previousBatch: String
        )
    }

    data class Presence(
            @JsonProperty("events")
            val events: List<Event<*>>
    )

    data class AccountData(
            @JsonProperty("events")
            val events: List<Event<*>>
    )

    data class DeviceLists(
            @JsonProperty("changed")
            val changed: List<String>,
            @JsonProperty("left")
            val left: List<String>
    )

    data class ToDevice(
            @JsonProperty("events")
            val events: List<Event<*>>
    )
}