package net.folivo.matrix.bot.membership

interface MembershipChangeService {
    suspend fun onRoomJoin(userId: String, roomId: String)
    suspend fun onRoomLeave(userId: String, roomId: String)
    suspend fun shouldJoinRoom(userId: String, roomId: String): Boolean
}