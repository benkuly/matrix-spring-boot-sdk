package net.folivo.matrix.bot.util

class BotServiceHelper(
        private val usersRegex: List<String>,
        private val roomsRegex: List<String>,
        private val username: String,
        private val serverName: String
) {
    fun isManagedUser(userId: String): Boolean {
        val username = userId.trimStart('@').substringBefore(":")
        return if (userId.substringAfter(":") == serverName)
            this.username == username || usersRegex.map { username.matches(Regex(it)) }.contains(true)
        else false
    }

    fun isManagedRoom(roomAlias: String): Boolean {
        val roomAliasName = roomAlias.trimStart('#').substringBefore(":")
        return if (roomAlias.substringAfter(":") == serverName)
            roomsRegex.map { roomAliasName.matches(Regex(it)) }.contains(true)
        else false
    }

    fun getBotUserId(): String {//FIXME test
        return "@${username}:${serverName}"
    }

}