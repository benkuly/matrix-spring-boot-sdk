package net.folivo.matrix.bot.membership

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.room.MatrixRoomService
import net.folivo.matrix.bot.user.MatrixUserService
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId
import net.folivo.matrix.restclient.MatrixClient
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

    override suspend fun onRoomJoin(userId: UserId, roomId: RoomId) {
        LOG.debug("save join in $roomId of user $userId")
        membershipService.getOrCreateMembership(userId = userId, roomId = roomId)
        membershipSyncService.syncRoomMemberships(roomId)
    }

    override suspend fun onRoomLeave(userId: UserId, roomId: RoomId) {
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
                                matrixClient.roomsApi.leaveRoom(roomId)
                            else matrixClient.roomsApi.leaveRoom(roomId, joinedUserId)
                            membershipService.deleteMembership(joinedUserId, roomId)
                        }
                }
                if (onlyManagedUsersLeft || noMembersLeft) {
                    roomService.deleteRoom(roomId)
                }
            }
        }
    }

    private suspend fun deleteUserWhenNotManaged(userId: UserId) {
        if (!userService.getOrCreateUser(userId).isManaged && membershipService.getMembershipsSizeByUserId(userId) == 0L) {
            LOG.debug("delete user $userId because there are no memberships left")
            userService.deleteUser(userId)
        }
    }

    override suspend fun shouldJoinRoom(userId: UserId, roomId: RoomId): Boolean {
        return true
    }
}