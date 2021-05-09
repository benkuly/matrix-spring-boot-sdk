package net.folivo.spring.matrix.bot.appservice

import io.ktor.http.*
import net.folivo.spring.matrix.bot.event.MatrixEventHandler
import net.folivo.spring.matrix.bot.membership.MembershipChangeHandler
import net.folivo.trixnity.appservice.rest.user.AppserviceUserService
import net.folivo.trixnity.client.rest.api.MatrixServerException
import net.folivo.trixnity.core.model.MatrixId
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class AppserviceMemberEventHandler(
    private val membershipChangeHandler: MembershipChangeHandler,
    private val appserviceUserService: AppserviceUserService,
) : MatrixEventHandler<MemberEventContent> {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun supports(): KClass<MemberEventContent> {
        return MemberEventContent::class
    }

    override suspend fun handleEvent(event: Event<out MemberEventContent>) {
        require(event is Event.StateEvent)
        val userId = MatrixId.UserId(event.stateKey)
        try {
            membershipChangeHandler.handleMembership(userId, event.roomId, event.content.membership)
        } catch (error: MatrixServerException) {
            if (error.statusCode == HttpStatusCode.Forbidden) {
                LOG.warn("try to register user because of ${error.errorResponse}")
                try {
                    appserviceUserService.registerManagedUser(userId)
                    membershipChangeHandler.handleMembership(userId, event.roomId, event.content.membership)
                } catch (registerError: MatrixServerException) {
                    if (registerError.statusCode == HttpStatusCode.Forbidden) {
                        LOG.warn("could not register user due to: ${error.errorResponse}")
                    }
                }
            }
        }
    }
}