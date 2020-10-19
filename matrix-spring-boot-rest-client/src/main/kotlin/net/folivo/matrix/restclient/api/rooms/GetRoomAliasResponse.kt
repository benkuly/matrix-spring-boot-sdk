package net.folivo.matrix.restclient.api.rooms

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.model.MatrixId.RoomId

data class GetRoomAliasResponse(
        @JsonProperty("room_id")
        val roomId: RoomId,
        @JsonProperty("servers")
        val servers: List<String>
)