package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService.RoomExistingState
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService.RoomExistingState.CAN_BE_CREATED
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService.RoomExistingState.EXISTS

open class DefaultMatrixAppserviceRoomService(
        private val helper: MatrixAppserviceServiceHelper
) : MatrixAppserviceRoomService {
    override suspend fun roomExistingState(roomAlias: String): RoomExistingState {
        return if (helper.isManagedUser(roomAlias)) CAN_BE_CREATED else EXISTS
    }

    override suspend fun getCreateRoomParameter(roomAlias: String): CreateRoomParameter {
        return CreateRoomParameter()
    }

    override suspend fun saveRoom(roomAlias: String, roomId: String) {
    }

    override suspend fun saveRoomJoin(roomId: String, userId: String) {
    }

    override suspend fun saveRoomLeave(roomId: String, userId: String) {
    }
}