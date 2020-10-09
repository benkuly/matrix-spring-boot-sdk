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
    override suspend fun onRoomJoin(roomId: String, userId: String) {
        LOG.debug("saveRoomJoin in room $roomId of user $userId")
        roomService.getOrCreateRoom(roomId)
        userService.getOrCreateUser(userId)
        membershipService.getOrCreateMembership(userId, roomId)
    }

    @Transactional
    override suspend fun onRoomLeave(roomId: String, userId: String) {
        val membershipsSize = membershipService.getMembershipsSizeByRoomId(roomId)
        if (membershipsSize > 1) {
            LOG.debug("save room leave in room $roomId of user $userId")
            membershipService.deleteMembership(userId, roomId)

            if (membershipService.hasRoomOnlyManagedUsersLeft(roomId)) {
                LOG.debug("leave room $roomId with all managed users because there are only managed users left")
                val memberships = membershipService.getMembershipsByRoomId(roomId)
                memberships
                        .map { it.userId }
                        .collect { joinedUserId ->
                            if (joinedUserId == helper.getBotUserId())
                                matrixClient.roomsApi.leaveRoom(roomId)
                            else matrixClient.roomsApi.leaveRoom(roomId, joinedUserId)
                        }
            }
        } else {
            LOG.debug("delete room $roomId and membership because there are no members left")
            membershipService.deleteMembership(userId, roomId)
            roomService.deleteRoom(roomId)
            if (membershipService.getMembershipsSizeByUserId(userId) == 0L) {
                LOG.debug("delete user $userId because there are no memberships left")
                userService.deleteUser(userId)
            }
        }
    }
}