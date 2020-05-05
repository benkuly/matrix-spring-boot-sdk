package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.user.CreateUserParameter
import reactor.core.publisher.Mono

interface AppserviceBotManager {
    fun shouldCreateUser(matrixUsername: String): Mono<Boolean>
    fun shouldCreateRoom(matrixRoomAlias: String): Mono<Boolean>
    fun getCreateRoomParameter(roomAliasName: String): Mono<CreateRoomParameter>
    fun getCreateUserParameter(username: String): Mono<CreateUserParameter>
}