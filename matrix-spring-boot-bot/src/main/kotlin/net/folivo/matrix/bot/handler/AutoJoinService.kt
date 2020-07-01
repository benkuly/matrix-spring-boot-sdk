package net.folivo.matrix.bot.handler

interface AutoJoinService {
    suspend fun shouldJoin(roomId: String, userId: String?, isAsUser: Boolean = false): Boolean
}