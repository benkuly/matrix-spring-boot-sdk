package net.folivo.matrix.bot.membership

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.membership.DefaultMembershipChangeService
import net.folivo.matrix.bot.room.MatrixRoom
import net.folivo.matrix.bot.room.MatrixRoomService
import net.folivo.matrix.bot.user.MatrixUser
import net.folivo.matrix.bot.user.MatrixUserService
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId
import net.folivo.matrix.restclient.MatrixClient

class AppserviceMembershipChangeServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {

        val roomServiceMock: MatrixRoomService = mockk(relaxed = true)
        val membershipServiceMock: MatrixMembershipService = mockk(relaxed = true)
        val userServiceMock: MatrixUserService = mockk(relaxed = true)
        val matrixClientMock: MatrixClient = mockk(relaxed = true)
        val botPropertiesMock: MatrixBotProperties = mockk()

        val cut = DefaultMembershipChangeService(
                roomServiceMock,
                membershipServiceMock,
                userServiceMock,
                matrixClientMock,
                botPropertiesMock
        )

        val botUser = UserId("bot", "server")
        val user1 = UserId("user1", "server")
        val user2 = UserId("user2", "server")
        val room = RoomId("room", "server")

        beforeTest {
            coEvery { botPropertiesMock.botUserId }.returns(botUser)
        }

        describe(DefaultMembershipChangeService::onRoomJoin.name) {
            it("should get or create user, room and membership") {
                cut.onRoomJoin(user1, room)

                coVerifyAll {
                    membershipServiceMock.getOrCreateMembership(user1, room)
                }
            }
        }
        describe(DefaultMembershipChangeService::onRoomLeave.name) {
            beforeTest {
                coEvery { roomServiceMock.getOrCreateRoom(room) }.returns(MatrixRoom(room))
            }
            it("should do nothing, when user is not in room") {
                coEvery { membershipServiceMock.doesRoomContainsMembers(any(), any()) }
                        .returns(false)
                cut.onRoomLeave(user1, room)
                coVerifyAll {
                    membershipServiceMock.doesRoomContainsMembers(
                            room,
                            match { it.contains(user1) && it.size == 1 })
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
                    coEvery { userServiceMock.getOrCreateUser(any()) }.returns(MatrixUser(user1, true))
                    cut.onRoomLeave(user1, room)
                    coVerify {
                        membershipServiceMock.deleteMembership(user1, room)
                    }
                }
                it("should delete user when not managed and does not have rooms") {
                    coEvery { userServiceMock.getOrCreateUser(any()) }.returns(MatrixUser(user1, false))
                    coEvery { membershipServiceMock.getMembershipsSizeByUserId(user1) }.returns(0L)
                    cut.onRoomLeave(user1, room)
                    coVerify {
                        userServiceMock.deleteUser(user1)
                    }
                }
                it("should not delete user when managed") {
                    coEvery { userServiceMock.getOrCreateUser(any()) }.returns(MatrixUser(user1, true))
                    cut.onRoomLeave(user1, room)
                    coVerify(exactly = 0) {
                        userServiceMock.deleteUser(user1)
                    }
                }
                it("should not delete user when member in other rooms") {
                    coEvery { userServiceMock.getOrCreateUser(any()) }.returns(MatrixUser(user1, false))
                    coEvery { membershipServiceMock.getMembershipsSizeByUserId(user1) }.returns(1L)
                    cut.onRoomLeave(user1, room)
                    coVerify(exactly = 0) {
                        userServiceMock.deleteUser(user1)
                    }
                }
            }
            describe("user is last member") {
                beforeTest {
                    coEvery { userServiceMock.getOrCreateUser(any()) }.returns(MatrixUser(user1, true))
                    coEvery { membershipServiceMock.doesRoomContainsMembers(any(), any()) }
                            .returns(true)
                    coEvery { membershipServiceMock.getMembershipsSizeByRoomId(any()) }
                            .returns(0L)
                    coEvery { membershipServiceMock.hasRoomOnlyManagedUsersLeft(any()) }
                            .returns(false)
                }
                it("should delete room when not managed") {
                    coEvery { roomServiceMock.getOrCreateRoom(any()) }.returns(MatrixRoom(room, false))
                    cut.onRoomLeave(user1, room)
                    coVerify { roomServiceMock.deleteRoom(room) }
                }
                it("should not delete room when managed") {
                    coEvery { roomServiceMock.getOrCreateRoom(any()) }.returns(MatrixRoom(room, true))
                    cut.onRoomLeave(user1, room)
                    coVerify(exactly = 0) { roomServiceMock.deleteRoom(any()) }
                }
            }
            describe("room has only managed users left") {
                beforeTest {
                    coEvery { userServiceMock.getOrCreateUser(user1) }.returns(MatrixUser(user1, true))
                    coEvery { membershipServiceMock.doesRoomContainsMembers(any(), any()) }
                            .returns(true)
                    coEvery { membershipServiceMock.getMembershipsSizeByRoomId(any()) }
                            .returns(2L)
                    coEvery { membershipServiceMock.hasRoomOnlyManagedUsersLeft(any()) }
                            .returns(true)
                    coEvery { membershipServiceMock.getMembershipsByRoomId(room) }
                            .returns(
                                    flowOf(
                                            MatrixMembership(user2, room),
                                            MatrixMembership(botUser, room)
                                    )
                            )
                }
                it("all members should leave room and their membership deleted") {
                    cut.onRoomLeave(user1, room)
                    coVerify {
                        matrixClientMock.roomsApi.leaveRoom(room, user2)
                        matrixClientMock.roomsApi.leaveRoom(room)
                        membershipServiceMock.deleteMembership(user2, room)
                        membershipServiceMock.deleteMembership(botUser, room)
                    }
                }
                it("should delete room when not managed") {
                    coEvery { roomServiceMock.getOrCreateRoom(any()) }.returns(MatrixRoom(room, false))
                    cut.onRoomLeave(user1, room)
                    coVerify { roomServiceMock.deleteRoom(room) }
                }
                it("should not delete room when managed") {
                    coEvery { roomServiceMock.getOrCreateRoom(any()) }.returns(MatrixRoom(room, true))
                    cut.onRoomLeave(user1, room)
                    coVerify(exactly = 0) { roomServiceMock.deleteRoom(any()) }
                }
            }
        }

        describe(DefaultMembershipChangeService::shouldJoinRoom.name) {
            it("should always return true") {
                cut.shouldJoinRoom(UserId("blub", "bla"), RoomId("bla", "blub")).shouldBeTrue()
            }
        }

        afterTest {
            clearMocks(
                    roomServiceMock,
                    membershipServiceMock,
                    userServiceMock,
                    matrixClientMock,
                    botPropertiesMock
            )
        }
    }
}