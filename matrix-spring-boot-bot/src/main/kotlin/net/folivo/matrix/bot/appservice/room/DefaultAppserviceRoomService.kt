package net.folivo.matrix.bot.appservice.room

import net.folivo.matrix.appservice.api.room.AppserviceRoomService
import net.folivo.matrix.appservice.api.room.AppserviceRoomService.RoomExistingState
import net.folivo.matrix.appservice.api.room.AppserviceRoomService.RoomExistingState.*
import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.core.model.MatrixId.RoomAliasId
import net.folivo.matrix.core.model.MatrixId.RoomId

open class DefaultAppserviceRoomService(
        private val roomService: MatrixRoomService,
        private val helper: BotServiceHelper
) : AppserviceRoomService {

    override suspend fun roomExistingState(roomAlias: RoomAliasId): RoomExistingState {
        val roomExists = roomService.existsByRoomAlias(roomAlias)
        return if (roomExists) EXISTS
        else if (helper.isManagedRoom(roomAlias)) CAN_BE_CREATED else DOES_NOT_EXISTS
    }

    override suspend fun getCreateRoomParameter(roomAlias: RoomAliasId): CreateRoomParameter {
        return CreateRoomParameter()
    }

    override suspend fun onCreatedRoom(roomAlias: RoomAliasId, roomId: RoomId) {
        roomService.getOrCreateRoomAlias(roomAlias, roomId)
    }
}