package net.folivo.matrix.appservice.api.room

interface AppserviceRoomService {

    enum class RoomExistingState {
        EXISTS, DOES_NOT_EXISTS, CAN_BE_CREATED
    }

    suspend fun roomExistingState(roomAlias: String): RoomExistingState
    suspend fun getCreateRoomParameter(roomAlias: String): CreateRoomParameter
    suspend fun onCreateRoom(roomAlias: String, roomId: String)
}