package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.user.CreateUserParameter
import reactor.core.publisher.Mono

open class DefaultAppserviceBotManager : AppserviceBotManager {
    override fun shouldCreateUser(matrixUserId: String): Mono<Boolean> {
        return Mono.just(true)
    }

    override fun shouldCreateRoom(matrixRoomAlias: String): Mono<Boolean> {
        return Mono.just(false)
    }

    override fun getCreateRoomParameter(matrixRoomAlias: String): Mono<CreateRoomParameter> {
        return Mono.just(CreateRoomParameter())
    }

    override fun getCreateUserParameter(matrixUserId: String): Mono<CreateUserParameter> {
        return Mono.just(CreateUserParameter())
    }
}