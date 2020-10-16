package net.folivo.matrix.bot.appservice.membership

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import net.folivo.matrix.bot.appservice.room.MatrixRoom
import net.folivo.matrix.bot.appservice.room.MatrixRoomService
import net.folivo.matrix.bot.appservice.user.MatrixUser
import net.folivo.matrix.bot.appservice.user.MatrixUserService
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.restclient.MatrixClient

class AppserviceMembershipChangeServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {

        val roomServiceMock: MatrixRoomService = mockk(relaxed = true)
        val membershipServiceMock: MatrixMembershipService = mockk(relaxed = true)
        val userServiceMock: MatrixUserService = mockk(relaxed = true)
        val matrixClientMock: MatrixClient = mockk(relaxed = true)
        val helperMock: BotServiceHelper = mockk(relaxed = true)

        val cut = AppserviceMembershipChangeService(
                roomServiceMock,
                membershipServiceMock,
                userServiceMock,
                matrixClientMock,
                helperMock
        )

        beforeTest {
            coEvery { helperMock.getBotUserId() }.returns("@bot:server")
        }

        describe(AppserviceMembershipChangeService::onRoomJoin.name) {
            it("should get or create user, room and membership") {
                cut.onRoomJoin("someUserId", "someRoomId")

                coVerifyAll {
                    membershipServiceMock.getOrCreateMembership("someUserId", "someRoomId")
                }
            }
        }
        describe(AppserviceMembershipChangeService::onRoomLeave.name) {
            beforeTest {
                coEvery { roomServiceMock.getOrCreateRoom("someRoomId") }.returns(MatrixRoom("someRoomId"))
            }
            it("should do nothing, when user is not in room") {
                coEvery { membershipServiceMock.doesRoomContainsMembers(any(), any()) }
                        .returns(false)
                cut.onRoomLeave("someUserId", "someRoomId")
                coVerifyAll {
                    membershipServiceMock.doesRoomContainsMembers(
                            "someRoomId",
                            match { it.contains("someUserId") && it.size == 1 })
                }
            }
            describe("user is member") {
                beforeTest {
                    coEvery { membershipServiceMock.doesRoomContainsMembers(any(), any()) }
                            .returns(true)
                    coEvery { membershipServiceMock.getMembershipsSizeByRoomId(any()) }
                            .returns(12L)
                    coEvery { membershipServiceMock.hasRoomOnlyManagedUsersLeft(any()) }
                            .returns(false)
                }
                it("should delete membership") {
                    coEvery { userServiceMock.getOrCreateUser(any()) }.returns(MatrixUser("someUserId", true))
                    cut.onRoomLeave("someUserId", "someRoomId")
                    coVerify {
                        membershipServiceMock.deleteMembership("someUserId", "someRoomId")
                    }
                }
                it("should delete user when not managed and does not have rooms") {
                    coEvery { userServiceMock.getOrCreateUser(any()) }.returns(MatrixUser("someUserId", false))
                    coEvery { membershipServiceMock.getMembershipsSizeByUserId("someUserId") }.returns(0L)
                    cut.onRoomLeave("someUserId", "someRoomId")
                    coVerify {
                        userServiceMock.deleteUser("someUserId")
                    }
                }
                it("should not delete user when managed") {
                    coEvery { userServiceMock.getOrCreateUser(any()) }.returns(MatrixUser("someUserId", true))
                    cut.onRoomLeave("someUserId", "someRoomId")
                    coVerify(exactly = 0) {
                        userServiceMock.deleteUser("someUserId")
                    }
                }
                it("should not delete user when member in other rooms") {
                    coEvery { userServiceMock.getOrCreateUser(any()) }.returns(MatrixUser("someUserId", false))
                    coEvery { membershipServiceMock.getMembershipsSizeByUserId("someUserId") }.returns(1L)
                    cut.onRoomLeave("someUserId", "someRoomId")
                    coVerify(exactly = 0) {
                        userServiceMock.deleteUser("someUserId")
                    }
                }
            }
            describe("user is last member") {
                beforeTest {
                    coEvery { userServiceMock.getOrCreateUser(any()) }.returns(MatrixUser("someUserId", true))
                    coEvery { membershipServiceMock.doesRoomContainsMembers(any(), any()) }
                            .returns(true)
                    coEvery { membershipServiceMock.getMembershipsSizeByRoomId(any()) }
                            .returns(0L)
                    coEvery { membershipServiceMock.hasRoomOnlyManagedUsersLeft(any()) }
                            .returns(false)
                }
                it("should delete room when not managed") {
                    coEvery { roomServiceMock.getOrCreateRoom(any()) }.returns(MatrixRoom("someRoomId", false))
                    cut.onRoomLeave("someUserId", "someRoomId")
                    coVerify { roomServiceMock.deleteRoom("someRoomId") }
                }
                it("should not delete room when managed") {
                    coEvery { roomServiceMock.getOrCreateRoom(any()) }.returns(MatrixRoom("someRoomId", true))
                    cut.onRoomLeave("someUserId", "someRoomId")
                    coVerify(exactly = 0) { roomServiceMock.deleteRoom(any()) }
                }
            }
            describe("room has only managed users left") {
                beforeTest {
                    coEvery { userServiceMock.getOrCreateUser("someUserId") }.returns(MatrixUser("someUserId", true))
                    coEvery { membershipServiceMock.doesRoomContainsMembers(any(), any()) }
                            .returns(true)
                    coEvery { membershipServiceMock.getMembershipsSizeByRoomId(any()) }
                            .returns(2L)
                    coEvery { membershipServiceMock.hasRoomOnlyManagedUsersLeft(any()) }
                            .returns(true)
                    coEvery { membershipServiceMock.getMembershipsByRoomId("someRoomId") }
                            .returns(
                                    flowOf(
                                            MatrixMembership("someUserId2", "someRoomId"),
                                            MatrixMembership("@bot:server", "someRoomId")
                                    )
                            )
                }
                it("all members should leave room and their membership deleted") {
                    cut.onRoomLeave("someUserId", "someRoomId")
                    coVerify {
                        matrixClientMock.roomsApi.leaveRoom("someRoomId", "someUserId2")
                        matrixClientMock.roomsApi.leaveRoom("someRoomId")
                        membershipServiceMock.deleteMembership("someUserId2", "someRoomId")
                        membershipServiceMock.deleteMembership("@bot:server", "someRoomId")
                    }
                }
                it("should delete room when not managed") {
                    coEvery { roomServiceMock.getOrCreateRoom(any()) }.returns(MatrixRoom("someRoomId", false))
                    cut.onRoomLeave("someUserId", "someRoomId")
                    coVerify { roomServiceMock.deleteRoom("someRoomId") }
                }
                it("should not delete room when managed") {
                    coEvery { roomServiceMock.getOrCreateRoom(any()) }.returns(MatrixRoom("someRoomId", true))
                    cut.onRoomLeave("someUserId", "someRoomId")
                    coVerify(exactly = 0) { roomServiceMock.deleteRoom(any()) }
                }
            }
        }

        describe(AppserviceMembershipChangeService::shouldJoinRoom.name) {
            it("should always return true") {
                cut.shouldJoinRoom("bla", "bla").shouldBeTrue()
            }
        }

        afterTest {
            clearMocks(
                    roomServiceMock,
                    membershipServiceMock,
                    userServiceMock,
                    matrixClientMock,
                    helperMock
            )
        }
    }
}