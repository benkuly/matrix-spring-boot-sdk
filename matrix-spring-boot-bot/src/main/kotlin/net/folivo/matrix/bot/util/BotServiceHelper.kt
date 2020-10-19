package net.folivo.matrix.bot.util

import net.folivo.matrix.appservice.config.AppserviceProperties
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.core.model.MatrixId.RoomAliasId
import net.folivo.matrix.core.model.MatrixId.UserId

class BotServiceHelper(
        private val botProperties: MatrixBotProperties,
        private val appserviceProperties: AppserviceProperties
) {
    fun isManagedUser(userId: UserId): Boolean {
        return if (userId.domain == botProperties.serverName)
            userId.localpart == botProperties.username || appserviceProperties.namespaces.users
                    .map { userId.localpart.matches(Regex(it.regex)) }.contains(true)
        else false
    }

    fun isManagedRoom(roomAlias: RoomAliasId): Boolean {
        return if (roomAlias.domain == botProperties.serverName)
            appserviceProperties.namespaces.rooms.map { roomAlias.localpart.matches(Regex(it.regex)) }.contains(true)
        else false
    }

    fun getBotUserId(): UserId {
        return UserId(botProperties.username, botProperties.serverName)
    }

}