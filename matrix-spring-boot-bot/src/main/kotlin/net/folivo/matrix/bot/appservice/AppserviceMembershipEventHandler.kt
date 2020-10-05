package net.folivo.matrix.bot.appservice

import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.bot.membership.MembershipHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import org.slf4j.LoggerFactory

class AppserviceMembershipEventHandler(//FIXME test
        private val membershipHandler: MembershipHandler
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
                LOG.warn("could not handle join event due to missing roomId")
                return
            }

            val userId = event.stateKey
            membershipHandler.handleMembership(roomId, userId, event.content.membership)
        }
    }
}