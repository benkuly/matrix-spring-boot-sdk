package net.folivo.matrix.appservice.api.room

class NoOpMatrixAppserviceRoomService : MatrixAppserviceRoomService {
    override fun roomExistingState(roomAlias: String): MatrixAppserviceRoomService.RoomExistingState {
        return MatrixAppserviceRoomService.RoomExistingState.DOES_NOT_EXISTS
    }

    override fun getCreateRoomParameter(roomAlias: String): CreateRoomParameter {
        return CreateRoomParameter()
    }

    override fun saveRoom(roomAlias: String, roomId: String) {
    }
}