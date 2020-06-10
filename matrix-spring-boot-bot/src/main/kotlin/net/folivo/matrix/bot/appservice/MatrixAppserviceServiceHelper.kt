package net.folivo.matrix.bot.appservice

import reactor.core.publisher.Mono

class MatrixAppserviceServiceHelper(
        private val usersRegex: List<String>,
        private val roomsRegex: List<String>,
        private val asUsername: String
) {
    fun isManagedUser(userId: String): Mono<Boolean> {
        return Mono.fromSupplier {
            val username = userId.trimStart('@').substringBefore(":")
            asUsername == username || usersRegex.map { username.matches(Regex(it)) }.contains(true)
        }
    }

    fun isManagedRoom(roomAlias: String): Mono<Boolean> {
        return Mono.fromSupplier {
            val roomAliasName = roomAlias.trimStart('#').substringBefore(":")
            roomsRegex.map { roomAliasName.matches(Regex(it)) }.contains(true)
        }
    }
}