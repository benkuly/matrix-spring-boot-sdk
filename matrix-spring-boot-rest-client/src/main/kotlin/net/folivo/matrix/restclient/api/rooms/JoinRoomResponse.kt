package net.folivo.matrix.restclient.api.rooms

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.model.MatrixId.RoomId

internal data class JoinRoomResponse(
        @JsonProperty("room_id")
        val roomId: RoomId
)