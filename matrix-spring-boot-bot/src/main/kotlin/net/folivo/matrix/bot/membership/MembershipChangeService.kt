package net.folivo.matrix.bot.membership

interface MembershipChangeService {
    suspend fun onRoomJoin(roomId: String, userId: String)
    suspend fun onRoomLeave(roomId: String, userId: String)
    suspend fun shouldJoinRoom(roomId: String, userId: String): Boolean
}