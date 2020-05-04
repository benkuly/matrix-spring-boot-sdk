package net.folivo.matrix.bot.appservice.room

import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.bot.appservice.AppserviceBotManager

// FIXME test
class DefaultMatrixAppserviceRoomService(
        private val appserviceBotManager: AppserviceBotManager,
        private val appserviceRoomRepository: AppserviceRoomRepository
) : MatrixAppserviceRoomService {

    override fun roomExistingState(roomAliasName: String): MatrixAppserviceRoomService.RoomExistingState {
        return if (appserviceRoomRepository.findByMatrixRoomAlias(roomAliasName) == null
                   && appserviceBotManager.shouldCreateRoom(roomAliasName)
        ) {
            MatrixAppserviceRoomService.RoomExistingState.CAN_BE_CREATED
        } else {
            MatrixAppserviceRoomService.RoomExistingState.DOES_NOT_EXISTS
        }
    }

    override fun getCreateRoomParameter(roomAliasName: String): CreateRoomParameter {
        return appserviceBotManager.getCreateRoomParameter(roomAliasName)
    }

    override fun saveRoom(roomAliasName: String) {
        appserviceRoomRepository.save(AppserviceRoom(roomAliasName))
    }
}