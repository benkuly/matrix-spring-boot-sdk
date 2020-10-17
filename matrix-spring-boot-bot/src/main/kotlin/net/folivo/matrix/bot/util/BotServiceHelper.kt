package net.folivo.matrix.bot.util

import net.folivo.matrix.appservice.config.AppserviceProperties
import net.folivo.matrix.bot.config.MatrixBotProperties

class BotServiceHelper(
        private val botProperties: MatrixBotProperties,
        private val appserviceProperties: AppserviceProperties
) {
    fun isManagedUser(userId: String): Boolean {
        val username = userId.trimStart('@').substringBefore(":")
        return if (userId.substringAfter(":") == botProperties.serverName)
            username == botProperties.username || appserviceProperties.namespaces.users
                    .map { userId.matches(Regex(it.regex)) }.contains(true)
        else false
    }

    fun isManagedRoom(roomAlias: String): Boolean {
        return if (roomAlias.substringAfter(":") == botProperties.serverName)
            appserviceProperties.namespaces.rooms.map { roomAlias.matches(Regex(it.regex)) }.contains(true)
        else false
    }

    fun getBotUserId(): String {
        return "@${botProperties.username}:${botProperties.serverName}"
    }

}