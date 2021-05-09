package net.folivo.spring.matrix.bot.membership

import kotlinx.coroutines.flow.collect
import net.folivo.spring.matrix.bot.config.MatrixBotProperties
import net.folivo.spring.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.ALL
import net.folivo.spring.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.NONE
import net.folivo.spring.matrix.bot.room.MatrixRoomService
import net.folivo.spring.matrix.bot.util.BotServiceHelper
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.client.rest.api.MatrixServerException
import net.folivo.trixnity.core.model.MatrixId
import net.folivo.trixnity.core.model.events.m.room.CanonicalAliasEventContent
import org.slf4j.LoggerFactory

class MatrixMembershipSyncService(
    private val matrixClient: MatrixClient,
    private val roomService: MatrixRoomService,
    private val membershipService: MatrixMembershipService,
    private val helper: BotServiceHelper,
    private val botProperties: MatrixBotProperties
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun syncBotRoomsAndMemberships() {
        try {
            matrixClient.room.getJoinedRooms()
                .collect { roomId ->
                    LOG.info("sync room $roomId")
                    try {
                        val roomAlias = matrixClient.room.getStateEvent<CanonicalAliasEventContent>(roomId).alias
                        if (roomAlias != null && helper.isManagedRoom(roomAlias)) {
                            LOG.debug("set room alias of room $roomId to $roomAlias")
                            roomService.getOrCreateRoomAlias(roomAlias, roomId)
                        }
                    } catch (error: MatrixServerException) {
                        LOG.debug("room $roomId seems to have no alias and therefore is not managed")
                    }
                    matrixClient.room.getJoinedMembers(roomId).joined.keys.forEach { userId ->
                        LOG.debug("save membership of $userId in $roomId")
                        membershipService.getOrCreateMembership(userId, roomId)
                    }
                }
            LOG.debug("synced bot user members")
        } catch (error: Throwable) {
            LOG.error("tried to sync bot user rooms, but that was not possible: ${error.message}")
        }
    }

    suspend fun syncRoomMemberships(roomId: MatrixId.RoomId) {
        // this is needed to get all members, e.g. when managed user joins a new room
        val trackMembershipMode = botProperties.trackMembership
        if (trackMembershipMode != NONE && membershipService.getMembershipsSizeByRoomId(roomId) == 1L) { // FIXME test
            LOG.debug("collect all members in room $roomId because we didn't saved it yet")
            try {
                matrixClient.room.getJoinedMembers(roomId).joined.keys
                    .forEach { joinedUserId ->
                        if (trackMembershipMode == ALL
                            || trackMembershipMode == MatrixBotProperties.TrackMembershipMode.MANAGED && helper.isManagedUser(
                                joinedUserId
                            )
                        )
                            membershipService.getOrCreateMembership(joinedUserId, roomId)
                    }
            } catch (error: Throwable) {
                LOG.error("tried to sync room $roomId, but that was not possible: ${error.message}")
            }
        }
    }
}