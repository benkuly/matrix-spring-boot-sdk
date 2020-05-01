package net.folivo.matrix.appservice.api

interface MatrixAppserviceRoomService {

    enum class RoomExistingState {
        EXISTS, DOES_NOT_EXISTS, CAN_BE_CREATED
    }

    fun roomExistingState(roomAlias: String): RoomExistingState
    fun getCreateRoomParameter(roomAlias: String): CreateRoomParameter
    fun saveRoom(roomAlias: String, roomId: String)
}