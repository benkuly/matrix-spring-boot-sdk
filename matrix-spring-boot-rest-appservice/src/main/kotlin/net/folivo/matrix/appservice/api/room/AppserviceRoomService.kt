package net.folivo.matrix.appservice.api.room

import net.folivo.matrix.core.model.MatrixId.RoomAliasId
import net.folivo.matrix.core.model.MatrixId.RoomId

interface AppserviceRoomService {

    enum class RoomExistingState {
        EXISTS, DOES_NOT_EXISTS, CAN_BE_CREATED
    }

    suspend fun roomExistingState(roomAlias: RoomAliasId): RoomExistingState
    suspend fun getCreateRoomParameter(roomAlias: RoomAliasId): CreateRoomParameter
    suspend fun onCreatedRoom(roomAlias: RoomAliasId, roomId: RoomId)
}