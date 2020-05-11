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
        if (autoJoin != MatrixBotProperties.AutoJoinMode.DISABLED
            && event is MemberEvent
            && event.content.membership == INVITE
        ) {
            if (roomId == null) {
                logger.warn("could not handle join event due to missing roomId")
                return Mono.empty()
            }

            val invitedUser = event.stateKey
            val isAsUser = invitedUser.trimStart('@').substringBefore(":") == asUsername
            val asUserId = if (isAsUser) null else invitedUser

            return if (autoJoin == MatrixBotProperties.AutoJoinMode.RESTRICTED
                       && roomId.substringAfter(":") != serverName
            ) {
                logger.warn("reject room invite of $invitedUser to $roomId because autoJoin is restricted to $serverName")
                matrixClient.roomsApi.leaveRoom(roomId = roomId, asUserId = asUserId)
            } else if (isAsUser || usersRegex.map { invitedUser.matches(Regex(it)) }.contains(true)) {
                logger.debug("join room $roomId with $invitedUser")
                matrixClient.roomsApi.joinRoom(roomIdOrAlias = roomId, asUserId = asUserId)
                        .flatMap { roomService.saveRoomJoin(it, invitedUser) }
            } else {
                logger.debug("invited user $invitedUser not managed by this application service")
                Mono.empty()
            }
        }
        return Mono.empty()
    }
}