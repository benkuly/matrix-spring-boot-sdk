package net.folivo.matrix.bot.appservice

class MatrixAppserviceServiceHelper(
        private val usersRegex: List<String>,
        private val roomsRegex: List<String>,
        private val asUsername: String
) {
    suspend fun isManagedUser(userId: String): Boolean {
        val username = userId.trimStart('@').substringBefore(":")
        return asUsername == username || usersRegex.map { username.matches(Regex(it)) }.contains(true)
    }

    suspend fun isManagedRoom(roomAlias: String): Boolean {
        val roomAliasName = roomAlias.trimStart('#').substringBefore(":")
        return roomsRegex.map { roomAliasName.matches(Regex(it)) }.contains(true)
    }
}