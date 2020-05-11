package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.user.CreateUserParameter
import reactor.core.publisher.Mono

interface AppserviceBotManager {
    fun shouldCreateUser(matrixUserId: String): Mono<Boolean>
    fun getCreateUserParameter(matrixUserId: String): Mono<CreateUserParameter>

    fun shouldCreateRoom(matrixRoomAlias: String): Mono<Boolean>
    fun getCreateRoomParameter(matrixRoomAlias: String): Mono<CreateRoomParameter>

}