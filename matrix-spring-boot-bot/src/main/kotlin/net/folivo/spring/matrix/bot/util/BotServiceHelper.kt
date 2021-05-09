package net.folivo.spring.matrix.bot.util

import net.folivo.spring.matrix.bot.config.MatrixBotProperties
import net.folivo.trixnity.core.model.MatrixId

class BotServiceHelper(
    private val botProperties: MatrixBotProperties,
    private val userNamespaceRegex: Set<Regex>,
    private val roomNamespaceRegex: Set<Regex>
) {
    fun isManagedUser(userId: MatrixId.UserId): Boolean {
        return if (userId.domain == botProperties.serverName)
            userId.localpart == botProperties.username || userNamespaceRegex.map { userId.localpart.matches(it) }
                .contains(true)
        else false
    }

    fun isManagedRoom(roomAlias: MatrixId.RoomAliasId): Boolean {
        return if (roomAlias.domain == botProperties.serverName)
            roomNamespaceRegex.map { roomAlias.localpart.matches(it) }.contains(true)
        else false
    }
}