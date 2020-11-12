package net.folivo.matrix.restclient.api.rooms

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.model.MatrixId.UserId

data class GetJoinedMembersResponse(
        @JsonProperty("joined")
        val joined: Map<UserId, RoomMember>
) {
    data class RoomMember(
            @JsonProperty("display_name")
            val displayName: String? = null,
            @JsonProperty("avatar_url")
            val avatarUrl: String? = null
    )
}