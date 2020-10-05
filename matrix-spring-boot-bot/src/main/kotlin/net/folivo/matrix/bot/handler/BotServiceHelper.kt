package net.folivo.matrix.bot.handler

class BotServiceHelper(
        private val usersRegex: List<String>,
        private val roomsRegex: List<String>,
        private val asUsername: String,
        private val serverName: String
) {
    fun isManagedUser(userId: String): Boolean {
        val username = userId.trimStart('@').substringBefore(":")
        return if (userId.substringAfter(":") == serverName)
            asUsername == username || usersRegex.map { username.matches(Regex(it)) }.contains(true)
        else false
    }

    fun isManagedRoom(roomAlias: String): Boolean {
        val roomAliasName = roomAlias.trimStart('#').substringBefore(":")
        return if (roomAlias.substringAfter(":") == serverName)
            roomsRegex.map { roomAliasName.matches(Regex(it)) }.contains(true)
        else false
    }

}