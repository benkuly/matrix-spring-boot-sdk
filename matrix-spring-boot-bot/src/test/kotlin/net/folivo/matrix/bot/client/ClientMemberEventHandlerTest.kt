package net.folivo.matrix.bot.client

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.matrix.bot.membership.MembershipChangeHandler
import net.folivo.matrix.core.model.MatrixId.*
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.INVITE
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberUnsignedData
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent

class ClientMemberEventHandlerTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val membershipChangeHandlerMock: MembershipChangeHandler = mockk(relaxed = true)

        val cut = ClientMemberEventHandler(membershipChangeHandlerMock)

        describe(ClientMemberEventHandler::supports.name) {
            it("should only support ${MemberEvent::class.simpleName}") {
                cut.supports(MemberEvent::class.java).shouldBeTrue()
                cut.supports(MessageEvent::class.java).shouldBeFalse()
            }
        }

        describe(ClientMemberEventHandler::handleEvent.name) {
            val event = MemberEvent(
                    MemberEventContent(membership = INVITE),
                    EventId("event", "server"),
                    UserId("sender", "server"),
                    123,
                    null,
                    MemberUnsignedData(),
                    UserId("invited", "server")
            )
            it("should delegate to ${MembershipChangeHandler::class.simpleName}") {
                cut.handleEvent(event, RoomId("room", "server"))
                coVerify {
                    membershipChangeHandlerMock.handleMembership(
                            UserId("invited", "server"), RoomId("room", "server"), INVITE
                    )
                }
            }
        }

        afterTest { clearMocks(membershipChangeHandlerMock) }
    }
}