package net.folivo.matrix.bot.handler

class DefaultAutoJoinService : AutoJoinService {
    override suspend fun shouldJoin(roomId: String, userId: String?, isAsUser: Boolean): Boolean {
        return true
    }
}