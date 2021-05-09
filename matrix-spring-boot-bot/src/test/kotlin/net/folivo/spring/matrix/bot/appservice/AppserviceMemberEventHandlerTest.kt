package net.folivo.spring.matrix.bot.appservice

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.spring.matrix.bot.membership.MembershipChangeHandler
import net.folivo.trixnity.appservice.rest.user.AppserviceUserService
import net.folivo.trixnity.client.rest.api.ErrorResponse
import net.folivo.trixnity.client.rest.api.MatrixServerException
import net.folivo.trixnity.core.model.MatrixId
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.UnsignedData
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent.Membership.INVITE

class AppserviceMemberEventHandlerTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val membershipChangeHandlerMock: MembershipChangeHandler = mockk(relaxed = true)
        val appserviceUserServiceMock: AppserviceUserService = mockk(relaxed = true)
        val cut = AppserviceMemberEventHandler(membershipChangeHandlerMock, appserviceUserServiceMock)

        describe(AppserviceMemberEventHandler::supports.name) {
            it("should support ${MemberEventContent::class.simpleName}") {
                cut.supports().shouldBe(MemberEventContent::class)
            }
        }

        describe(AppserviceMemberEventHandler::handleEvent.name) {
            val event = Event.StateEvent(
                MemberEventContent(membership = INVITE),
                MatrixId.EventId("event", "server"),
                MatrixId.UserId("sender", "server"),
                MatrixId.RoomId("room", "server"),
                123,
                UnsignedData(),
                MatrixId.UserId("invitedUser", "server").toString()
            )
            it("should delegate to ${MembershipChangeHandler::class.simpleName}") {
                cut.handleEvent(event)
                coVerify {
                    membershipChangeHandlerMock.handleMembership(
                        MatrixId.UserId("invitedUser", "server"),
                        MatrixId.RoomId("room", "server"),
                        INVITE
                    )
                }
            }
            describe("delegate to ${MembershipChangeHandler::class.simpleName} fails with ${MatrixServerException::class.simpleName} and ${HttpStatusCode.Forbidden}") {
                coEvery { membershipChangeHandlerMock.handleMembership(any(), any(), any()) }
                    .throws(MatrixServerException(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN")))
                it("should try to register user") {
                    cut.handleEvent(event)
                    coVerify {
                        appserviceUserServiceMock.registerManagedUser(MatrixId.UserId("invitedUser", "server"))
                        membershipChangeHandlerMock.handleMembership(
                            MatrixId.UserId("invitedUser", "server"),
                            MatrixId.RoomId("room", "server"),
                            INVITE
                        )
                    }
                }
            }
        }
    }
}