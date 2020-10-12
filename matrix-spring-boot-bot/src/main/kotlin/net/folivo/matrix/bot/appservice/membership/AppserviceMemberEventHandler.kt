package net.folivo.matrix.bot.appservice.membership

import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.bot.membership.MembershipChangeHandler
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.FORBIDDEN

class AppserviceMemberEventHandler(
        //FIXME test
        private val membershipChangeHandler: MembershipChangeHandler,
        private val appserviceHelper: AppserviceHandlerHelper,
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
            try {
                membershipChangeHandler.handleMembership(roomId, userId, event.content.membership)
            } catch (error: MatrixServerException) {
                if (error.statusCode == FORBIDDEN) {
                    LOG.warn("try to register user because of ${error.errorResponse}")
                    try {
                        appserviceHelper.registerManagedUser(userId)
                        membershipChangeHandler.handleMembership(roomId, userId, event.content.membership)
                    } catch (registerError: MatrixServerException) {
                        if (registerError.statusCode == FORBIDDEN) {
                            LOG.warn("could not register user due to: ${error.errorResponse}")
                        }
                    }
                }
            }
        }
    }
}