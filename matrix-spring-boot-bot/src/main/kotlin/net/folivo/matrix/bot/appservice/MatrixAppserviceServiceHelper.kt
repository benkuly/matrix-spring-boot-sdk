package net.folivo.matrix.bot.appservice

import reactor.core.publisher.Mono

class MatrixAppserviceServiceHelper(
        private val usersRegex: List<String>,
        private val roomsRegex: List<String>
) {
    fun shouldCreateUser(matrixUserId: String): Mono<Boolean> {
        return Mono.fromSupplier {
            val username = matrixUserId.trimStart('@').substringBefore(":")
            usersRegex.map { username.matches(Regex(it)) }.contains(true)
        }
    }

    fun shouldCreateRoom(matrixRoomAlias: String): Mono<Boolean> {
        return Mono.fromSupplier {
            val roomAliasName = matrixRoomAlias.trimStart('#').substringBefore(":")
            roomsRegex.map { roomAliasName.matches(Regex(it)) }.contains(true)
        }
    }
}