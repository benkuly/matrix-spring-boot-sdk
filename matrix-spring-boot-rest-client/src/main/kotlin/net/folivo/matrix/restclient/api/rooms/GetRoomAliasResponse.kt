package net.folivo.matrix.restclient.api.rooms

import com.fasterxml.jackson.annotation.JsonProperty

data class GetRoomAliasResponse(
        @JsonProperty("room_id")
        val roomId: String,
        @JsonProperty("servers")
        val servers: List<String>
)