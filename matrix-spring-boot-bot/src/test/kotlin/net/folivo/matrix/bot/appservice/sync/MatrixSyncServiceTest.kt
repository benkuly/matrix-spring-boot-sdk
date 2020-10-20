package net.folivo.matrix.bot.appservice.sync

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.*
import net.folivo.matrix.bot.membership.MatrixMembershipService
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId
import net.folivo.matrix.restclient.MatrixClient

class MatrixSyncServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val matrixClientMock: MatrixClient = mockk()
        val membershipServiceMock: MatrixMembershipService = mockk()
        val helperMock: BotServiceHelper = mockk()
        val botPropertiesMock: MatrixBotProperties = mockk()

        val roomId1 = RoomId("room1", "server")
        val roomId2 = RoomId("room2", "server")
        val userId1 = UserId("user1", "server")
        val userId2 = UserId("user2", "server")
        val cut = MatrixSyncService(matrixClientMock, membershipServiceMock, helperMock, botPropertiesMock)

        describe(MatrixSyncService::syncBotMemberships.name) {
            it("should create membership for each room and member") {
                coEvery { matrixClientMock.roomsApi.getJoinedRooms() }
                        .returns(flowOf(roomId1, roomId2))
                coEvery { matrixClientMock.roomsApi.getJoinedMembers(roomId1).joined.keys }
                        .returns(setOf(userId1))
                coEvery { matrixClientMock.roomsApi.getJoinedMembers(roomId2).joined.keys }
                        .returns(setOf(userId1, userId2))
                coEvery { membershipServiceMock.getOrCreateMembership(any(), any()) }.returns(mockk())

                cut.syncBotMemberships()

                coVerify {
                    membershipServiceMock.getOrCreateMembership(userId1, roomId1)
                    membershipServiceMock.getOrCreateMembership(userId1, roomId2)
                    membershipServiceMock.getOrCreateMembership(userId2, roomId2)
                }
            }
        }

        describe(MatrixSyncService::syncRoomMemberships.name) {
            it("should fetch all members when there are no memberships for this rooms") {
                coEvery { botPropertiesMock.trackMembership }.returns(ALL)
                coEvery { membershipServiceMock.getMembershipsSizeByRoomId(roomId1) }.returns(0L)
                coEvery { matrixClientMock.roomsApi.getJoinedMembers(roomId1).joined.keys }
                        .returns(setOf(userId1, userId2))
                coEvery { membershipServiceMock.getOrCreateMembership(any(), any()) }.returns(mockk())

                cut.syncRoomMemberships(roomId1)

                coVerify {
                    membershipServiceMock.getOrCreateMembership(userId1, roomId1)
                    membershipServiceMock.getOrCreateMembership(userId2, roomId1)
                }
            }
            it("should fetch only managed members when there are no memberships for this rooms") {
                coEvery { botPropertiesMock.trackMembership }.returns(MANAGED)
                coEvery { helperMock.isManagedUser(any()) }.returnsMany(true, false)
                coEvery { membershipServiceMock.getMembershipsSizeByRoomId(roomId1) }.returns(0L)
                coEvery { matrixClientMock.roomsApi.getJoinedMembers(roomId1).joined.keys }
                        .returns(setOf(userId1, userId2))
                coEvery { membershipServiceMock.getOrCreateMembership(any(), any()) }.returns(mockk())

                cut.syncRoomMemberships(roomId1)

                coVerify {
                    membershipServiceMock.getOrCreateMembership(userId1, roomId1)
                }
            }
            it("should not fetch members when there are memberships for this rooms") {
                coEvery { botPropertiesMock.trackMembership }.returns(ALL)
                coEvery { membershipServiceMock.getMembershipsSizeByRoomId(roomId1) }.returns(2L)

                cut.syncRoomMemberships(roomId1)

                coVerify(exactly = 0) {
                    membershipServiceMock.getOrCreateMembership(any(), any())
                }
            }
            it("should not fetch members when not wanted") {
                coEvery { botPropertiesMock.trackMembership }.returns(NONE)
                coEvery { membershipServiceMock.getMembershipsSizeByRoomId(roomId1) }.returns(0L)

                cut.syncRoomMemberships(roomId1)

                coVerify(exactly = 0) {
                    membershipServiceMock.getOrCreateMembership(any(), any())
                }
            }
        }

        afterTest { clearMocks(matrixClientMock, membershipServiceMock) }
    }
}