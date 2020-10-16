package net.folivo.matrix.bot.appservice.membership

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.bot.membership.MembershipChangeHandler
import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.INVITE
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberUnsignedData
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import org.springframework.http.HttpStatus.FORBIDDEN

class AppserviceMemberEventHandlerTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val membershipChangeHandlerMock: MembershipChangeHandler = mockk(relaxed = true)
        val appserviceHelperMock: AppserviceHandlerHelper = mockk(relaxed = true)
        val cut = AppserviceMemberEventHandler(membershipChangeHandlerMock, appserviceHelperMock)

        describe(AppserviceMemberEventHandler::supports.name) {
            it("should support ${MemberEvent::class.simpleName}") {
                cut.supports(MemberEvent::class.java).shouldBeTrue()
            }
            it("should not support ${MessageEvent::class.simpleName}") {
                cut.supports(MessageEvent::class.java).shouldBeFalse()
            }
        }

        describe(AppserviceMemberEventHandler::handleEvent.name) {
            val event = MemberEvent(
                    MemberEventContent(membership = INVITE),
                    "someId",
                    "someSender",
                    123,
                    null,
                    MemberUnsignedData(),
                    "someInvitedUserId"
            )
            it("should delegate to ${MembershipChangeHandler::class.simpleName}") {
                cut.handleEvent(event, "someRoomId")
                coVerify { membershipChangeHandlerMock.handleMembership("someInvitedUserId", "someRoomId", INVITE) }
            }
            describe("delegate to ${MembershipChangeHandler::class.simpleName} fails with ${MatrixServerException::class.simpleName} and $FORBIDDEN") {
                coEvery { membershipChangeHandlerMock.handleMembership(any(), any(), any()) }
                        .throws(MatrixServerException(FORBIDDEN, ErrorResponse("FORBIDDEN")))
                it("should try to register user") {
                    cut.handleEvent(event, "someRoomId")
                    coVerify {
                        appserviceHelperMock.registerManagedUser("someInvitedUserId")
                        membershipChangeHandlerMock.handleMembership("someInvitedUserId", "someRoomId", INVITE)
                    }
                }
            }
        }
    }
}