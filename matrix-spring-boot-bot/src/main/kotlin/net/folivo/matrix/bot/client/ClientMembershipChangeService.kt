package net.folivo.matrix.bot.client

import net.folivo.matrix.bot.membership.MembershipChangeService
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId

class ClientMembershipChangeService : MembershipChangeService {

    override suspend fun onRoomJoin(userId: UserId, roomId: RoomId) {
    }

    override suspend fun onRoomLeave(userId: UserId, roomId: RoomId) {
    }

    override suspend fun shouldJoinRoom(userId: UserId, roomId: RoomId): Boolean {
        return true
    }
}