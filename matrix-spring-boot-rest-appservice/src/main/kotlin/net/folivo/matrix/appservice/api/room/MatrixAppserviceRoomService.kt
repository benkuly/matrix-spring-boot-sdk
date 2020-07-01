package net.folivo.matrix.appservice.api.room

interface MatrixAppserviceRoomService {

    enum class RoomExistingState {
        EXISTS, DOES_NOT_EXISTS, CAN_BE_CREATED
    }

    suspend fun roomExistingState(roomAlias: String): RoomExistingState
    suspend fun getCreateRoomParameter(roomAlias: String): CreateRoomParameter
    suspend fun saveRoom(roomAlias: String, roomId: String)

    suspend fun saveRoomJoin(roomId: String, userId: String)
    suspend fun saveRoomLeave(roomId: String, userId: String)
}