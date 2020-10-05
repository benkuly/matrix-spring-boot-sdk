package net.folivo.matrix.bot.membership

interface AutoJoinCustomizer {
    suspend fun shouldJoin(roomId: String, userId: String): Boolean
}