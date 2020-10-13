package net.folivo.matrix.bot.client

import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.bot.membership.MembershipChangeHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import org.slf4j.LoggerFactory

class ClientMemberEventHandler(
        private val membershipChangeHandler: MembershipChangeHandler
) : MatrixEventHandler {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override fun supports(clazz: Class<*>): Boolean {
        return clazz == MemberEvent::class.java
    }

    override suspend fun handleEvent(event: Event<*>, roomId: String?) {
        if (event is MemberEvent) {
            if (roomId == null) {
                LOG.warn("could not handle member event due to missing roomId")
                return
            }

            val userId = event.stateKey
            membershipChangeHandler.handleMembership(roomId, userId, event.content.membership)
        }
    }
}