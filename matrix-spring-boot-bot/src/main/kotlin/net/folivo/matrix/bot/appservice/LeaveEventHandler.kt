package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.BAN
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.LEAVE
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

class LeaveEventHandler(
        private val roomService: MatrixAppserviceRoomService,
        private val asUsername: String,
        private val usersRegex: List<String>
) : MatrixEventHandler {

    private val logger = LoggerFactory.getLogger(LeaveEventHandler::class.java)

    override fun supports(clazz: Class<*>): Boolean {
        return clazz == MemberEvent::class.java
    }

    override fun handleEvent(event: Event<*>, roomId: String?): Mono<Void> {
        if (event is MemberEvent
            && (event.content.membership == LEAVE
                || event.content.membership == BAN)
        ) {
            if (roomId == null) {
                logger.warn("could not handle leave event due to missing roomId")
                return Mono.empty()
            }


            val leavedUser = event.stateKey
            val leavedUsername = leavedUser.trimStart('@').substringBefore(":")
            val isAsUser = leavedUsername == asUsername

            if (isAsUser || usersRegex.map { leavedUsername.matches(Regex(it)) }.contains(true)) {
                logger.debug("handle room leave of user $leavedUser and room $roomId")
                return roomService.saveRoomLeave(roomId, leavedUser)
            }
            logger.debug("ignore leave event due to unmanaged user $leavedUser")
        }
        return Mono.empty()
    }
}