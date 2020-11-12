package net.folivo.matrix.bot.appservice.sync

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.matrix.bot.membership.MatrixMembershipSyncService
import net.folivo.matrix.bot.room.MatrixRoomService
import net.folivo.matrix.bot.user.MatrixUserService

class InitialSyncServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val userServiceMock: MatrixUserService = mockk(relaxed = true)
        val roomServiceMock: MatrixRoomService = mockk(relaxed = true)
        val membershipSyncServiceMock: MatrixMembershipSyncService = mockk(relaxed = true)

        val cut = InitialSyncService(userServiceMock, roomServiceMock, membershipSyncServiceMock)

        describe(InitialSyncService::initialSync.name) {
            cut.initialSync()
            it("should delete all rooms") {
                coVerify { roomServiceMock.deleteAllRooms() }
            }
            it("should delete all users") {
                coVerify { userServiceMock.deleteAllUsers() }
            }
            it("should sync rooms of bot user") {
                coVerify { membershipSyncServiceMock.syncBotRoomsAndMemberships() }
            }
        }
    }
}