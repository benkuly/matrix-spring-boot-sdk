package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.user.CreateUserParameter
import reactor.core.publisher.Mono

open class DefaultAppserviceBotManager(
        private val usersRegex: List<String>,
        private val roomsRegex: List<String>
) : AppserviceBotManager {
    override fun shouldCreateUser(matrixUserId: String): Mono<Boolean> {
        return Mono.fromSupplier {
            val username = matrixUserId.trimStart('@').substringBefore(":")
            usersRegex.map { username.matches(Regex(it)) }.contains(true)
        }
    }

    override fun shouldCreateRoom(matrixRoomAlias: String): Mono<Boolean> {
        return Mono.fromSupplier {
            val roomAliasName = matrixRoomAlias.trimStart('#').substringBefore(":")
            roomsRegex.map { roomAliasName.matches(Regex(it)) }.contains(true)
        }
    }

    override fun getCreateRoomParameter(matrixRoomAlias: String): Mono<CreateRoomParameter> {
        return Mono.just(CreateRoomParameter())
    }

    override fun getCreateUserParameter(matrixUserId: String): Mono<CreateUserParameter> {
        return Mono.just(CreateUserParameter())
    }
}