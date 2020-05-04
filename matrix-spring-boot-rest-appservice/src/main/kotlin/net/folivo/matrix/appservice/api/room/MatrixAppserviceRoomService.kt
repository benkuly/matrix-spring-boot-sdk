package net.folivo.matrix.appservice.api.room

interface MatrixAppserviceRoomService {

    enum class RoomExistingState {
        EXISTS, DOES_NOT_EXISTS, CAN_BE_CREATED
    }

    fun roomExistingState(roomAliasName: String): RoomExistingState
    fun getCreateRoomParameter(roomAliasName: String): CreateRoomParameter
    fun saveRoom(roomAliasName: String)
}