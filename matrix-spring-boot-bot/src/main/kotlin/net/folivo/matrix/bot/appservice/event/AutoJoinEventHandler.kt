package net.folivo.matrix.bot.appservice.event

import net.folivo.matrix.bot.appservice.room.DefaultMatrixAppserviceRoomService
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.INVITE
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

// FIXME test
class AutoJoinEventHandler(
        private val matrixClient: MatrixClient,
        private val roomService: DefaultMatrixAppserviceRoomService,
        private val asUsername: String,
        private val usersRegex: List<String>,
        private val autoJoin: MatrixBotProperties.AutoJoinMode,
        private val serverName: String
) : MatrixEventHandler {

    private val logger = LoggerFactory.getLogger(AutoJoinEventHandler::class.java)

    override fun supports(clazz: Class<*>): Boolean {
        return clazz == MemberEvent::class.java
    }

    override fun handleEvent(event: Event<*>, roomId: String?): Mono<Void> {
        // FIXME reject foreign server (instead of autojoin Boolean maybe TRUE/FALSE/RESTRICTED
        if (autoJoin != MatrixBotProperties.AutoJoinMode.DISABLED
            && event is MemberEvent
            && event.content.membership == INVITE
        ) {
            if (roomId == null) {
                logger.warn("could not handle join event due to missing roomId")
                return Mono.empty()
            }
            if (autoJoin == MatrixBotProperties.AutoJoinMode.RESTRICTED
                && roomId.substringAfter(":") != serverName
            ) {
                logger.warn("reject room invite to ${event.roomId} because autoJoin is restricted to $serverName")
                return matrixClient.roomsApi.leaveRoom(roomId, event.stateKey)
            }
            val invitedUser = event.stateKey
            if (invitedUser.trimStart('@').substringBefore(":") == asUsername) {
                logger.debug("join room $roomId with $invitedUser")
                return matrixClient.roomsApi.joinRoom(roomId)
                        .flatMap { roomService.saveRoomJoin(roomId, invitedUser) }
            } else if (usersRegex.map { invitedUser.matches(Regex(it)) }.contains(true)) {
                logger.debug("join room $roomId with $invitedUser")
                return matrixClient.roomsApi.joinRoom(roomIdOrAlias = roomId, asUserId = invitedUser)
                        .flatMap { roomService.saveRoomJoin(roomId, invitedUser) }
            } else {
                logger.debug("invited user $invitedUser not managed by this application service")
            }
        }
        return Mono.empty()
    }
}