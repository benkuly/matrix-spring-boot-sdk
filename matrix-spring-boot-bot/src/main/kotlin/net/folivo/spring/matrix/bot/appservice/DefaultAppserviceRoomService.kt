package net.folivo.spring.matrix.bot.appservice

import net.folivo.spring.matrix.bot.room.MatrixRoomService
import net.folivo.spring.matrix.bot.util.BotServiceHelper
import net.folivo.trixnity.appservice.rest.room.AppserviceRoomService
import net.folivo.trixnity.appservice.rest.room.AppserviceRoomService.RoomExistingState.*
import net.folivo.trixnity.appservice.rest.room.CreateRoomParameter
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.core.model.MatrixId

open class DefaultAppserviceRoomService(
    private val roomService: MatrixRoomService,
    private val helper: BotServiceHelper,
    override val matrixClient: MatrixClient
) : AppserviceRoomService {

    override suspend fun roomExistingState(roomAlias: MatrixId.RoomAliasId): AppserviceRoomService.RoomExistingState {
        val roomExists = roomService.existsByRoomAlias(roomAlias)
        return if (roomExists) EXISTS
        else if (helper.isManagedRoom(roomAlias)) CAN_BE_CREATED else DOES_NOT_EXISTS
    }

    override suspend fun getCreateRoomParameter(roomAlias: MatrixId.RoomAliasId): CreateRoomParameter {
        return CreateRoomParameter()
    }

    override suspend fun onCreatedRoom(roomAlias: MatrixId.RoomAliasId, roomId: MatrixId.RoomId) {
        roomService.getOrCreateRoomAlias(roomAlias, roomId)
    }
}