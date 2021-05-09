package net.folivo.spring.matrix.bot.membership

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import net.folivo.spring.matrix.bot.config.MatrixBotProperties
import net.folivo.spring.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.ALL
import net.folivo.spring.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.NONE
import net.folivo.spring.matrix.bot.room.MatrixRoomService
import net.folivo.spring.matrix.bot.util.BotServiceHelper
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.client.rest.api.room.GetJoinedMembersResponse
import net.folivo.trixnity.core.model.MatrixId
import net.folivo.trixnity.core.model.events.m.room.CanonicalAliasEventContent

class MatrixSyncServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val matrixClientMock: MatrixClient = mockk()
        val roomServiceMock: MatrixRoomService = mockk()
        val membershipServiceMock: MatrixMembershipService = mockk()
        val helperMock: BotServiceHelper = mockk()
        val botPropertiesMock: MatrixBotProperties = mockk()

        val roomId1 = MatrixId.RoomId("room1", "server")
        val roomId2 = MatrixId.RoomId("room2", "server")
        val roomAlias2 = MatrixId.RoomAliasId("alias2", "server")
        val userId1 = MatrixId.UserId("user1", "server")
        val userId2 = MatrixId.UserId("user2", "server")
        val cut = MatrixMembershipSyncService(
            matrixClientMock,
            roomServiceMock,
            membershipServiceMock,
            helperMock,
            botPropertiesMock
        )

        describe(MatrixMembershipSyncService::syncBotRoomsAndMemberships.name) {
            it("should create membership for each room and member") {
                coEvery { matrixClientMock.room.getJoinedRooms() }
                    .returns(flowOf(roomId1, roomId2))
                coEvery { matrixClientMock.room.getJoinedMembers(roomId1) }
                    .returns(GetJoinedMembersResponse(mapOf(userId1 to GetJoinedMembersResponse.RoomMember())))
                coEvery { matrixClientMock.room.getJoinedMembers(roomId2) }
                    .returns(
                        GetJoinedMembersResponse(
                            mapOf(
                                userId1 to GetJoinedMembersResponse.RoomMember(),
                                userId2 to GetJoinedMembersResponse.RoomMember()
                            )
                        )
                    )
                coEvery { matrixClientMock.room.getStateEvent(CanonicalAliasEventContent::class, roomId1) }
                    .returns(CanonicalAliasEventContent())
                coEvery { matrixClientMock.room.getStateEvent(CanonicalAliasEventContent::class, roomId2) }
                    .returns(CanonicalAliasEventContent(roomAlias2))
                coEvery { membershipServiceMock.getOrCreateMembership(any(), any()) }.returns(mockk())
                coEvery { roomServiceMock.getOrCreateRoomAlias(any(), any()) }.returns(mockk())
                coEvery { helperMock.isManagedRoom(roomAlias2) }.returns(true)

                cut.syncBotRoomsAndMemberships()

                coVerify {
                    membershipServiceMock.getOrCreateMembership(userId1, roomId1)
                    membershipServiceMock.getOrCreateMembership(userId1, roomId2)
                    membershipServiceMock.getOrCreateMembership(userId2, roomId2)
                    roomServiceMock.getOrCreateRoomAlias(roomAlias2, roomId2)
                }
            }
        }

        describe(MatrixMembershipSyncService::syncRoomMemberships.name) {
            it("should fetch all members when there are no memberships for this rooms") {
                coEvery { botPropertiesMock.trackMembership }.returns(ALL)
                coEvery { membershipServiceMock.getMembershipsSizeByRoomId(roomId1) }.returns(1L)
                coEvery { matrixClientMock.room.getJoinedMembers(roomId1) }
                    .returns(
                        GetJoinedMembersResponse(
                            mapOf(
                                userId1 to GetJoinedMembersResponse.RoomMember(),
                                userId2 to GetJoinedMembersResponse.RoomMember()
                            )
                        )
                    )
                coEvery { membershipServiceMock.getOrCreateMembership(any(), any()) }.returns(mockk())

                cut.syncRoomMemberships(roomId1)

                coVerify {
                    membershipServiceMock.getOrCreateMembership(userId1, roomId1)
                    membershipServiceMock.getOrCreateMembership(userId2, roomId1)
                }
            }
            it("should fetch only managed members when there are no memberships for this rooms") {
                coEvery { botPropertiesMock.trackMembership }.returns(MatrixBotProperties.TrackMembershipMode.MANAGED)
                coEvery { helperMock.isManagedUser(any()) }.returnsMany(true, false)
                coEvery { membershipServiceMock.getMembershipsSizeByRoomId(roomId1) }.returns(1L)
                coEvery { matrixClientMock.room.getJoinedMembers(roomId1) }
                    .returns(
                        GetJoinedMembersResponse(
                            mapOf(
                                userId1 to GetJoinedMembersResponse.RoomMember(),
                                userId2 to GetJoinedMembersResponse.RoomMember()
                            )
                        )
                    )
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
                coEvery { membershipServiceMock.getMembershipsSizeByRoomId(roomId1) }.returns(1L)

                cut.syncRoomMemberships(roomId1)

                coVerify(exactly = 0) {
                    membershipServiceMock.getOrCreateMembership(any(), any())
                }
            }
        }

        afterTest { clearMocks(matrixClientMock, roomServiceMock, membershipServiceMock, helperMock) }
    }
}