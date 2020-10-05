package net.folivo.matrix.bot.membership

interface MembershipService {
    suspend fun onRoomJoin(roomId: String, userId: String)
    suspend fun onRoomLeave(roomId: String, userId: String)
}