package net.folivo.matrix.bot.appservice.membership

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import net.folivo.matrix.bot.appservice.room.MatrixRoomService
import net.folivo.matrix.bot.appservice.user.MatrixUserService
import net.folivo.matrix.bot.membership.MembershipChangeService
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

class AppserviceMembershipChangeService(
        private val roomService: MatrixRoomService,
        private val membershipService: MatrixMembershipService,
        private val userService: MatrixUserService,
        private val matrixClient: MatrixClient,
        private val helper: BotServiceHelper
) : MembershipChangeService { //FIXME test

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    @Transactional
    override suspend fun onRoomJoin(userId: String, roomId: String) {
        LOG.debug("join in $roomId of user $userId")
        membershipService.getOrCreateMembership(userId = userId, roomId = roomId)
    }

    @Transactional
    override suspend fun onRoomLeave(userId: String, roomId: String) {
        // we do this here because it also syncs the room if it is not in database
        if (membershipService.doesRoomContainsMembers(roomId, setOf(userId))) {

            LOG.debug("save room leave in room $roomId of user $userId")
            membershipService.deleteMembership(userId, roomId)

            deleteUserWhenNotManaged(userId)

            val noMembersLeft = membershipService.getMembershipsSizeByRoomId(roomId) == 0L
            val onlyManagedUsersLeft = membershipService.hasRoomOnlyManagedUsersLeft(roomId)

            if (onlyManagedUsersLeft) {
                LOG.debug("leave room $roomId with all managed users because there are only managed users left")
                val memberships = membershipService.getMembershipsByRoomId(roomId)
                memberships
                        .map { it.userId }
                        .collect { joinedUserId ->
                            if (joinedUserId == helper.getBotUserId())
                                matrixClient.roomsApi.leaveRoom(roomId)
                            else matrixClient.roomsApi.leaveRoom(roomId, joinedUserId)
                            membershipService.deleteMembership(joinedUserId, roomId)
                            deleteUserWhenNotManaged(joinedUserId)
                        }
            }
            if (!roomService.getOrCreateRoom(roomId).isManaged && (onlyManagedUsersLeft || noMembersLeft)) {
                roomService.deleteRoom(roomId)
            }

        }
    }

    private suspend fun deleteUserWhenNotManaged(userId: String) {
        if (!userService.getOrCreateUser(userId).isManaged && membershipService.getMembershipsSizeByUserId(userId) == 0L) {
            LOG.debug("delete user $userId because there are no memberships left")
            userService.deleteUser(userId)
        }
    }

    override suspend fun shouldJoinRoom(userId: String, roomId: String): Boolean {
        return true
    }
}