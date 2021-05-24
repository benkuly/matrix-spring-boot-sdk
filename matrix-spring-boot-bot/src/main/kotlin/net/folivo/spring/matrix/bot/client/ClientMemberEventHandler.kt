package net.folivo.spring.matrix.bot.client

import net.folivo.spring.matrix.bot.event.MatrixEventHandler
import net.folivo.spring.matrix.bot.membership.MembershipChangeHandler
import net.folivo.trixnity.core.model.MatrixId
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent
import kotlin.reflect.KClass

class ClientMemberEventHandler(
    private val membershipChangeHandler: MembershipChangeHandler
) : MatrixEventHandler<MemberEventContent> {

    override fun supports(): KClass<MemberEventContent> {
        return MemberEventContent::class
    }

    override suspend fun handleEvent(event: Event<out MemberEventContent>) {
        if (event is Event.StateEvent) {
            membershipChangeHandler.handleMembership(
                MatrixId.UserId(event.stateKey),
                event.roomId,
                event.content.membership
            )
        } else if (event is Event.StrippedStateEvent) {
            membershipChangeHandler.handleMembership(
                MatrixId.UserId(event.stateKey),
                event.roomId,
                event.content.membership
            )
        }
    }
}