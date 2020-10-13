package net.folivo.matrix.bot.appservice.sync

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import net.folivo.matrix.bot.appservice.membership.MatrixMembershipService
import net.folivo.matrix.restclient.MatrixClient

class MatrixSyncServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val matrixClientMock: MatrixClient = mockk()
        val membershipService: MatrixMembershipService = mockk()

        val cut = MatrixSyncService(matrixClientMock, membershipService)

        describe(MatrixSyncService::syncBotMemberships.name) {
            it("should create membership for each room and member") {
                coEvery { matrixClientMock.roomsApi.getJoinedRooms() }
                        .returns(flowOf("roomId1", "roomId2"))
                coEvery { matrixClientMock.roomsApi.getJoinedMembers("roomId1").joined.keys }
                        .returns(setOf("userId1"))
                coEvery { matrixClientMock.roomsApi.getJoinedMembers("roomId2").joined.keys }
                        .returns(setOf("userId1", "userId2"))
                coEvery { membershipService.getOrCreateMembership(any(), any()) }.returns(mockk())

                cut.syncBotMemberships()

                coVerify {
                    membershipService.getOrCreateMembership("roomId1", "userId1")
                    membershipService.getOrCreateMembership("roomId2", "userId1")
                    membershipService.getOrCreateMembership("roomId2", "userId2")
                }
            }
        }

        describe(MatrixSyncService::syncRoomMemberships.name) {
            describe("there are memberships for this rooms") {
                coEvery { membershipService.getMembershipsSizeByRoomId("roomId") }.returns(2L)
                coEvery { matrixClientMock.roomsApi.getJoinedMembers("someRoomId").joined.keys }
                        .returns(setOf("userId1", "userId2"))

                cut.syncRoomMemberships("roomId")

                coVerify {
                    membershipService.getOrCreateMembership("roomId", "userId1")
                    membershipService.getOrCreateMembership("roomId", "userId2")
                }
            }
            describe("there are no memberships for this rooms") {
                coEvery { membershipService.getMembershipsSizeByRoomId("roomId") }.returns(0L)

                cut.syncRoomMemberships("roomId")

                coVerify(exactly = 0) {
                    membershipService.getOrCreateMembership(any(), any())
                }
            }
        }
    }
}