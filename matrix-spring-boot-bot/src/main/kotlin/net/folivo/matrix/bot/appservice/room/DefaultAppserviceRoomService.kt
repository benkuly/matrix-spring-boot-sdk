package net.folivo.matrix.bot.appservice.room

import net.folivo.matrix.appservice.api.room.AppserviceRoomService
import net.folivo.matrix.appservice.api.room.AppserviceRoomService.RoomExistingState
import net.folivo.matrix.appservice.api.room.AppserviceRoomService.RoomExistingState.*
import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.bot.util.BotServiceHelper

open class DefaultAppserviceRoomService(
        private val roomService: MatrixRoomService,
        private val helper: BotServiceHelper
) : AppserviceRoomService {

    override suspend fun roomExistingState(roomAlias: String): RoomExistingState {
        val roomExists = roomService.existsByRoomAlias(roomAlias)
        return if (roomExists) EXISTS
        else if (helper.isManagedRoom(roomAlias)) CAN_BE_CREATED else DOES_NOT_EXISTS
    }

    override suspend fun getCreateRoomParameter(roomAlias: String): CreateRoomParameter {
        return CreateRoomParameter()
    }

    override suspend fun onCreateRoom(roomAlias: String, roomId: String) {
        roomService.getOrCreateRoomAlias(roomAlias, roomId)
    }
}