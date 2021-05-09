package net.folivo.spring.matrix.bot.membership

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import net.folivo.spring.matrix.bot.config.MatrixBotProperties
import net.folivo.spring.matrix.bot.room.MatrixRoom
import net.folivo.spring.matrix.bot.room.MatrixRoomService
import net.folivo.spring.matrix.bot.user.MatrixUser
import net.folivo.spring.matrix.bot.user.MatrixUserService
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.core.model.MatrixId

class AppserviceMembershipChangeServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {

        val roomServiceMock: MatrixRoomService = mockk(relaxed = true)
        val membershipServiceMock: MatrixMembershipService = mockk(relaxed = true)
        val userServiceMock: MatrixUserService = mockk(relaxed = true)
        val membershipSyncServiceMock: MatrixMembershipSyncService = mockk(relaxed = true)
        val matrixClientMock: MatrixClient = mockk(relaxed = true)
        val botPropertiesMock: MatrixBotProperties = mockk()

        val cut = DefaultMembershipChangeService(
            roomServiceMock,
            membershipServiceMock,
            userServiceMock,
            membershipSyncServiceMock,
            matrixClientMock,
            botPropertiesMock
        )

        val botUserId = MatrixId.UserId("bot", "server")
        val userId1 = MatrixId.UserId("user1", "server")
        val userId2 = MatrixId.UserId("user2", "server")
        val roomId = MatrixId.RoomId("room", "server")

        beforeTest {
            coEvery { botPropertiesMock.botUserId }.returns(botUserId)
        }

        describe(DefaultMembershipChangeService::onRoomJoin.name) {
            it("should get or create user, room and membership") {
                cut.onRoomJoin(userId1, roomId)

                coVerifyOrder {
                    membershipServiceMock.getOrCreateMembership(userId1, roomId)
                    membershipSyncServiceMock.syncRoomMemberships(roomId)
                }
            }
        }
        describe(DefaultMembershipChangeService::onRoomLeave.name) {
            beforeTest {
                coEvery { roomServiceMock.getOrCreateRoom(roomId) }.returns(MatrixRoom(roomId))
            }
            it("should do nothing, when user is not in room") {
                coEvery { membershipServiceMock.doesRoomContainsMembers(any(), any()) }
                    .returns(false)
                cut.onRoomLeave(userId1, roomId)
                coVerifyAll {
                    membershipServiceMock.doesRoomContainsMembers(
                        roomId,
                        match { it.contains(userId1) && it.size == 1 })
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
                    coEvery { userServiceMock.getOrCreateUser(any()) }.returns(MatrixUser(userId1, true))
                    cut.onRoomLeave(userId1, roomId)
                    coVerify {
                        membershipServiceMock.deleteMembership(userId1, roomId)
                    }
                }
                it("should delete user when not managed and does not have rooms") {
                    coEvery { userServiceMock.getOrCreateUser(any()) }.returns(MatrixUser(userId1, false))
                    coEvery { membershipServiceMock.getMembershipsSizeByUserId(userId1) }.returns(0L)
                    cut.onRoomLeave(userId1, roomId)
                    coVerify {
                        userServiceMock.deleteUser(userId1)
                    }
                }
                it("should not delete user when managed") {
                    coEvery { userServiceMock.getOrCreateUser(any()) }.returns(MatrixUser(userId1, true))
                    cut.onRoomLeave(userId1, roomId)
                    coVerify(exactly = 0) {
                        userServiceMock.deleteUser(userId1)
                    }
                }
                it("should not delete user when member in other rooms") {
                    coEvery { userServiceMock.getOrCreateUser(any()) }.returns(MatrixUser(userId1, false))
                    coEvery { membershipServiceMock.getMembershipsSizeByUserId(userId1) }.returns(1L)
                    cut.onRoomLeave(userId1, roomId)
                    coVerify(exactly = 0) {
                        userServiceMock.deleteUser(userId1)
                    }
                }
            }
            describe("user is last member") {
                beforeTest {
                    coEvery { userServiceMock.getOrCreateUser(any()) }.returns(MatrixUser(userId1, true))
                    coEvery { membershipServiceMock.doesRoomContainsMembers(any(), any()) }
                        .returns(true)
                    coEvery { membershipServiceMock.getMembershipsSizeByRoomId(any()) }
                        .returns(0L)
                    coEvery { membershipServiceMock.hasRoomOnlyManagedUsersLeft(any()) }
                        .returns(false)
                }
                it("should delete room when not managed") {
                    coEvery { roomServiceMock.getOrCreateRoom(any()) }.returns(MatrixRoom(roomId, false))
                    cut.onRoomLeave(userId1, roomId)
                    coVerify { roomServiceMock.deleteRoom(roomId) }
                }
                it("should not delete room when managed") {
                    coEvery { roomServiceMock.getOrCreateRoom(any()) }.returns(MatrixRoom(roomId, true))
                    cut.onRoomLeave(userId1, roomId)
                    coVerify(exactly = 0) { roomServiceMock.deleteRoom(any()) }
                }
            }
            describe("room has only managed users left") {
                beforeTest {
                    coEvery { userServiceMock.getOrCreateUser(userId1) }.returns(MatrixUser(userId1, true))
                    coEvery { membershipServiceMock.doesRoomContainsMembers(any(), any()) }
                        .returns(true)
                    coEvery { membershipServiceMock.getMembershipsSizeByRoomId(any()) }
                        .returns(2L)
                    coEvery { membershipServiceMock.hasRoomOnlyManagedUsersLeft(any()) }
                        .returns(true)
                    coEvery { membershipServiceMock.getMembershipsByRoomId(roomId) }
                        .returns(
                            flowOf(
                                MatrixMembership(userId2, roomId),
                                MatrixMembership(botUserId, roomId)
                            )
                        )
                }
                describe("room is managed") {
                    beforeTest {
                        coEvery { roomServiceMock.getOrCreateRoom(any()) }.returns(MatrixRoom(roomId, true))
                    }
                    it("no members should leave room") {
                        cut.onRoomLeave(userId1, roomId)
                        coVerify(exactly = 0) {
                            matrixClientMock.room.leaveRoom(any(), any())
                        }
                    }
                    it("should not delete room") {
                        cut.onRoomLeave(userId1, roomId)
                        coVerify(exactly = 0) { roomServiceMock.deleteRoom(any()) }
                    }
                }
                describe("room is not managed") {
                    beforeTest {
                        coEvery { roomServiceMock.getOrCreateRoom(any()) }.returns(MatrixRoom(roomId, false))
                    }
                    it("all members should leave room and their membership deleted") {
                        cut.onRoomLeave(userId1, roomId)
                        coVerify {
                            matrixClientMock.room.leaveRoom(roomId, userId2)
                            matrixClientMock.room.leaveRoom(roomId)
                            membershipServiceMock.deleteMembership(userId2, roomId)
                            membershipServiceMock.deleteMembership(botUserId, roomId)
                        }
                    }
                    it("should delete room") {
                        cut.onRoomLeave(userId1, roomId)
                        coVerify { roomServiceMock.deleteRoom(roomId) }
                    }
                }
            }
        }

        describe(DefaultMembershipChangeService::shouldJoinRoom.name) {
            it("should always return true") {
                cut.shouldJoinRoom(MatrixId.UserId("blub", "bla"), MatrixId.RoomId("bla", "blub")).shouldBeTrue()
            }
        }

        afterTest {
            clearMocks(
                roomServiceMock,
                membershipServiceMock,
                userServiceMock,
                membershipSyncServiceMock,
                matrixClientMock,
                botPropertiesMock
            )
        }
    }
}