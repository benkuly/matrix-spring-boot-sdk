package net.folivo.matrix.bot.membership

import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId

interface MembershipChangeService {
    suspend fun onRoomJoin(userId: UserId, roomId: RoomId)
    suspend fun onRoomLeave(userId: UserId, roomId: RoomId)
    suspend fun shouldJoinRoom(userId: UserId, roomId: RoomId): Boolean
}