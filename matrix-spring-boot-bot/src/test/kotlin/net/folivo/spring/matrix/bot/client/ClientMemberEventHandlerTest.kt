package net.folivo.spring.matrix.bot.client

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.spring.matrix.bot.membership.MembershipChangeHandler
import net.folivo.trixnity.core.model.MatrixId
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.UnsignedData
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent.Membership.INVITE

class ClientMemberEventHandlerTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val membershipChangeHandlerMock: MembershipChangeHandler = mockk(relaxed = true)

        val cut = ClientMemberEventHandler(membershipChangeHandlerMock)

        describe(ClientMemberEventHandler::supports.name) {
            it("should only support ${MemberEventContent::class.simpleName}") {
                cut.supports().shouldBe(MemberEventContent::class)
            }
        }

        describe(ClientMemberEventHandler::handleEvent.name) {
            val event = Event.StateEvent(
                MemberEventContent(membership = INVITE),
                MatrixId.EventId("event", "server"),
                MatrixId.UserId("sender", "server"),
                MatrixId.RoomId("room", "server"),
                123,
                UnsignedData(),
                MatrixId.UserId("invited", "server").toString()
            )
            it("should delegate to ${MembershipChangeHandler::class.simpleName}") {
                cut.handleEvent(event)
                coVerify {
                    membershipChangeHandlerMock.handleMembership(
                        MatrixId.UserId("invited", "server"), MatrixId.RoomId("room", "server"),
                        INVITE
                    )
                }
            }
        }

        afterTest { clearMocks(membershipChangeHandlerMock) }
    }
}