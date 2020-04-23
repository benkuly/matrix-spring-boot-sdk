package net.folivo.matrix.restclient.api.rooms

import com.fasterxml.jackson.annotation.JsonProperty

data class GetJoinedMembersResponse(
        @JsonProperty("joined")
        val joined: Map<String, RoomMember>
) {
    data class RoomMember(
            @JsonProperty("display_name")
            val displayName: String? = null,
            @JsonProperty("avatar_url")
            val avatarUrl: String? = null
    )
}