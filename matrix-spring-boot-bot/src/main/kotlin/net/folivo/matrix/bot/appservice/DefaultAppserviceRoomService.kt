package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.room.AppserviceRoomService
import net.folivo.matrix.appservice.api.room.AppserviceRoomService.RoomExistingState
import net.folivo.matrix.appservice.api.room.AppserviceRoomService.RoomExistingState.CAN_BE_CREATED
import net.folivo.matrix.appservice.api.room.AppserviceRoomService.RoomExistingState.EXISTS
import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.bot.handler.BotServiceHelper

open class DefaultAppserviceRoomService(
        private val helper: BotServiceHelper
) : AppserviceRoomService {
    override suspend fun roomExistingState(roomAlias: String): RoomExistingState {
        return if (helper.isManagedUser(roomAlias)) CAN_BE_CREATED else EXISTS
    }

    override suspend fun getCreateRoomParameter(roomAlias: String): CreateRoomParameter {
        return CreateRoomParameter()
    }

    override suspend fun onCreateRoom(roomAlias: String, roomId: String) {
    }

}