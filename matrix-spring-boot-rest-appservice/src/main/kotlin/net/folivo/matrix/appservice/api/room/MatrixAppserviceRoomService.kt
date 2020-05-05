package net.folivo.matrix.appservice.api.room

import reactor.core.publisher.Mono

interface MatrixAppserviceRoomService {

    enum class RoomExistingState {
        EXISTS, DOES_NOT_EXISTS, CAN_BE_CREATED
    }

    fun roomExistingState(roomAlias: String): Mono<RoomExistingState>
    fun getCreateRoomParameter(roomAlias: String): Mono<CreateRoomParameter>
    fun saveRoom(roomAlias: String, roomId: String): Mono<Void>
}