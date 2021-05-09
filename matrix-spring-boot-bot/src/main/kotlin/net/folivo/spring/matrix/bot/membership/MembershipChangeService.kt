package net.folivo.spring.matrix.bot.membership

import net.folivo.trixnity.core.model.MatrixId

interface MembershipChangeService {
    suspend fun onRoomJoin(userId: MatrixId.UserId, roomId: MatrixId.RoomId)
    suspend fun onRoomLeave(userId: MatrixId.UserId, roomId: MatrixId.RoomId)
    suspend fun shouldJoinRoom(userId: MatrixId.UserId, roomId: MatrixId.RoomId): Boolean
}