package net.folivo.matrix.bot.client

import net.folivo.matrix.bot.membership.MembershipChangeService

class ClientMembershipChangeService : MembershipChangeService {

    override suspend fun onRoomJoin(userId: String, roomId: String) {
    }

    override suspend fun onRoomLeave(userId: String, roomId: String) {
    }

    override suspend fun shouldJoinRoom(userId: String, roomId: String): Boolean {
        return true
    }
}