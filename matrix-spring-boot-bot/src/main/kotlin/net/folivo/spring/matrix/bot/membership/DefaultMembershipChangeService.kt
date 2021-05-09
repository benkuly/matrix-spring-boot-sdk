package net.folivo.spring.matrix.bot.membership

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import net.folivo.spring.matrix.bot.config.MatrixBotProperties
import net.folivo.spring.matrix.bot.room.MatrixRoomService
import net.folivo.spring.matrix.bot.user.MatrixUserService
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.core.model.MatrixId
import org.slf4j.LoggerFactory

open class DefaultMembershipChangeService(
    private val roomService: MatrixRoomService,
    private val membershipService: MatrixMembershipService,
    private val userService: MatrixUserService,
    private val membershipSyncService: MatrixMembershipSyncService,
    private val matrixClient: MatrixClient,
    private val botProperties: MatrixBotProperties
) : MembershipChangeService {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun onRoomJoin(userId: MatrixId.UserId, roomId: MatrixId.RoomId) {
        LOG.debug("save join in $roomId of user $userId")
        membershipService.getOrCreateMembership(userId = userId, roomId = roomId)
        membershipSyncService.syncRoomMemberships(roomId)
    }

    override suspend fun onRoomLeave(userId: MatrixId.UserId, roomId: MatrixId.RoomId) {
        if (membershipService.doesRoomContainsMembers(roomId, setOf(userId))) {

            LOG.debug("save room leave in room $roomId of user $userId")
            membershipService.deleteMembership(userId, roomId)

            deleteUserWhenNotManaged(userId)

            val noMembersLeft = membershipService.getMembershipsSizeByRoomId(roomId) == 0L
            val onlyManagedUsersLeft = membershipService.hasRoomOnlyManagedUsersLeft(roomId)
            val isManaged = roomService.getOrCreateRoom(roomId).isManaged

            if (!isManaged) {
                if (onlyManagedUsersLeft) {
                    LOG.debug("leave room $roomId with all managed users because there are only managed users left")
                    val memberships = membershipService.getMembershipsByRoomId(roomId)
                    memberships
                        .map { it.userId }
                        .collect { joinedUserId ->
                            if (joinedUserId == botProperties.botUserId)
                                matrixClient.room.leaveRoom(roomId)
                            else matrixClient.room.leaveRoom(roomId, joinedUserId)
                            membershipService.deleteMembership(joinedUserId, roomId)
                        }
                }
                if (onlyManagedUsersLeft || noMembersLeft) {
                    roomService.deleteRoom(roomId)
                }
            }
        }
    }

    private suspend fun deleteUserWhenNotManaged(userId: MatrixId.UserId) {
        if (!userService.getOrCreateUser(userId).isManaged && membershipService.getMembershipsSizeByUserId(userId) == 0L) {
            LOG.debug("delete user $userId because there are no memberships left")
            userService.deleteUser(userId)
        }
    }

    override suspend fun shouldJoinRoom(userId: MatrixId.UserId, roomId: MatrixId.RoomId): Boolean {
        return true
    }
}