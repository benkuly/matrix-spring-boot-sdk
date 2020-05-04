package net.folivo.matrix.bot.appservice.room

import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.bot.appservice.AppserviceBotManager

// FIXME test
class DefaultMatrixAppserviceRoomService(
        private val appserviceBotManager: AppserviceBotManager,
        private val appserviceRoomRepository: AppserviceRoomRepository
) : MatrixAppserviceRoomService {

    override fun roomExistingState(roomAlias: String): MatrixAppserviceRoomService.RoomExistingState {
        val matrixRoomAlias = roomAlias.trimStart('#').substringBefore(":")
        return if (appserviceRoomRepository.findByMatrixRoomAlias(matrixRoomAlias) == null
                   && appserviceBotManager.shouldCreateRoom(matrixRoomAlias)
        ) {
            MatrixAppserviceRoomService.RoomExistingState.CAN_BE_CREATED
        } else {
            MatrixAppserviceRoomService.RoomExistingState.DOES_NOT_EXISTS
        }
    }

    override fun getCreateRoomParameter(roomAlias: String): CreateRoomParameter {
        TODO("Not yet implemented")
    }

    override fun saveRoom(roomAlias: String, roomId: String) {
        TODO("Not yet implemented")
    }
}