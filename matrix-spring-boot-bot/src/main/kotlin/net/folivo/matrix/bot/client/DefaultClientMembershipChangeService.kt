package net.folivo.matrix.bot.client

import net.folivo.matrix.bot.membership.MembershipChangeService

class DefaultClientMembershipChangeService(
) : MembershipChangeService {

    override suspend fun onRoomJoin(roomId: String, userId: String) {
    }

    override suspend fun onRoomLeave(roomId: String, userId: String) {
    }

    override suspend fun shouldJoinRoom(roomId: String, userId: String): Boolean {
        return true
    }
}