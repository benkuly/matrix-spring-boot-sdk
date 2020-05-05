package net.folivo.matrix.bot.appservice.event

import net.folivo.matrix.bot.appservice.room.DefaultMatrixAppserviceRoomService
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.INVITE
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory

// FIXME test
class AutoJoinEventHandler(
        private val matrixClient: MatrixClient,
        private val roomService: DefaultMatrixAppserviceRoomService,
        private val asUsername: String,
        private val usersRegex: List<String>
) : MatrixEventHandler {

    private val logger = LoggerFactory.getLogger(AutoJoinEventHandler::class.java)


    override fun supports(clazz: Class<*>): Boolean {
        return clazz == MemberEvent::class.java
    }

    override fun handleEvent(event: Event<*>, roomId: String?) {
        // FIXME reject foreign server (instead of autojoin Boolean maybe TRUE/FALSE/RESTRICTED
        if (event is MemberEvent && event.content.membership == INVITE) {
            if (roomId == null) {
                logger.warn("could not handle join event due to missing roomId")
                return
            }
            val invitedUser = event.stateKey
            if (invitedUser.trimStart('@').substringBefore(":") == asUsername) {
                logger.debug("join room $roomId with $invitedUser")
                matrixClient.roomsApi.joinRoom(roomId)
                        .doOnSuccess { roomService.saveRoomJoin(roomId, invitedUser) }
                        .block()
            } else if (usersRegex.map { invitedUser.matches(Regex(it)) }.contains(true)) {
                logger.debug("join room $roomId with $invitedUser")
                matrixClient.roomsApi.joinRoom(roomIdOrAlias = roomId, asUserId = invitedUser)
                        .doOnSuccess { roomService.saveRoomJoin(roomId, invitedUser) }
                        .block()
            } else {
                logger.debug("didn't found matching user $invitedUser")
            }
        }
    }
}