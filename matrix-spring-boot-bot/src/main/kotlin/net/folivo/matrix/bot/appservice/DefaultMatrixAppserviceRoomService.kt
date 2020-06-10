package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService.RoomExistingState
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService.RoomExistingState.CAN_BE_CREATED
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService.RoomExistingState.EXISTS
import reactor.core.publisher.Mono

open class DefaultMatrixAppserviceRoomService(
        private val helper: MatrixAppserviceServiceHelper
) : MatrixAppserviceRoomService {
    override fun roomExistingState(roomAlias: String): Mono<RoomExistingState> {
        return helper.isManagedUser(roomAlias)
                .map { if (it) CAN_BE_CREATED else EXISTS }
    }

    override fun getCreateRoomParameter(roomAlias: String): Mono<CreateRoomParameter> {
        return Mono.just(CreateRoomParameter())
    }

    override fun saveRoom(roomAlias: String, roomId: String): Mono<Void> {
        return Mono.empty()
    }

    override fun saveRoomJoin(roomId: String, userId: String): Mono<Void> {
        return Mono.empty()
    }

    override fun saveRoomLeave(roomId: String, userId: String): Mono<Void> {
        return Mono.empty()
    }
}