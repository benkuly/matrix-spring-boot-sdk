package net.folivo.matrix.restclient.api.rooms

import com.fasterxml.jackson.annotation.JsonProperty

internal data class GetJoinedRoomsResponse(
        @JsonProperty("joined_rooms")
        val joinedRooms: List<String>
)