package net.folivo.matrix.bot.membership

class DefaultAutoJoinCustomizer : AutoJoinCustomizer {
    override suspend fun shouldJoin(roomId: String, userId: String): Boolean {
        return true
    }
}