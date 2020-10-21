package net.folivo.matrix.bot.util

import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.core.model.MatrixId.RoomAliasId
import net.folivo.matrix.core.model.MatrixId.UserId

class BotServiceHelper(
        private val botProperties: MatrixBotProperties,
        private val userNamespaceRegex: Set<Regex>,
        private val roomNamespaceRegex: Set<Regex>
) {
    fun isManagedUser(userId: UserId): Boolean {
        return if (userId.domain == botProperties.serverName)
            userId.localpart == botProperties.username || userNamespaceRegex.map { userId.localpart.matches(it) }
                    .contains(true)
        else false
    }

    fun isManagedRoom(roomAlias: RoomAliasId): Boolean {
        return if (roomAlias.domain == botProperties.serverName)
            roomNamespaceRegex.map { roomAlias.localpart.matches(it) }.contains(true)
        else false
    }
}