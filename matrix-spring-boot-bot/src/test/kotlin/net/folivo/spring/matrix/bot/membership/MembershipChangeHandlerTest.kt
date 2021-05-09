package net.folivo.spring.matrix.bot.membership

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.*
import net.folivo.spring.matrix.bot.config.MatrixBotProperties
import net.folivo.spring.matrix.bot.config.MatrixBotProperties.AutoJoinMode.DISABLED
import net.folivo.spring.matrix.bot.config.MatrixBotProperties.AutoJoinMode.ENABLED
import net.folivo.spring.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.ALL
import net.folivo.spring.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.NONE
import net.folivo.spring.matrix.bot.util.BotServiceHelper
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.core.model.MatrixId
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent.Membership.*

class MembershipChangeHandlerTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val matrixClientMock: MatrixClient = mockk(relaxed = true)
        val membershipChangeServiceMock: MembershipChangeService = mockk(relaxed = true)
        val botHelperMock: BotServiceHelper = mockk()
        val botPropertiesMock: MatrixBotProperties = mockk {
            every { serverName } returns "server"
        }

        val cut = MembershipChangeHandler(
            matrixClientMock,
            membershipChangeServiceMock,
            botHelperMock,
            botPropertiesMock
        )

        val botUserId = MatrixId.UserId("bot", "server")
        val userId = MatrixId.UserId("user", "server")
        val roomId = MatrixId.RoomId("room", "server")
        val roomOnForeignServerId = MatrixId.RoomId("room", "foreignServer")

        beforeTest {
            every { botPropertiesMock.botUserId }.returns(botUserId)
            every { botHelperMock.isManagedUser(any()) }.returns(true)
            every { botPropertiesMock.serverName }.returns("server")
            coEvery { membershipChangeServiceMock.shouldJoinRoom(any(), any()) }.returns(true)
        }

        describe(MembershipChangeHandler::handleMembership.name) {
            describe("membership is $INVITE") {
                it("should do nothing when autoJoin is $DISABLED") {
                    every { botPropertiesMock.autoJoin }.returns(DISABLED)
                    cut.handleMembership(userId, roomId, INVITE)
                    coVerify {
                        matrixClientMock wasNot Called
                    }
                }
                it("should do nothing when user is not managed") {
                    every { botPropertiesMock.autoJoin }.returns(ENABLED)
                    every { botHelperMock.isManagedUser(userId) }.returns(false)
                    cut.handleMembership(userId, roomId, INVITE)
                    coVerify {
                        matrixClientMock wasNot Called
                    }
                }
                it("should leave room when autoJoin to foreign server is ${MatrixBotProperties.AutoJoinMode.RESTRICTED}") {
                    every { botPropertiesMock.autoJoin }.returns(MatrixBotProperties.AutoJoinMode.RESTRICTED)
                    cut.handleMembership(userId, roomOnForeignServerId, INVITE)
                    coVerify {
                        matrixClientMock.room.leaveRoom(roomOnForeignServerId, asUserId = userId)
                    }
                }
                it("should join room when autoJoin is ${MatrixBotProperties.AutoJoinMode.RESTRICTED}, but server is allowed") {
                    every { botPropertiesMock.autoJoin }.returns(MatrixBotProperties.AutoJoinMode.RESTRICTED)
                    cut.handleMembership(userId, roomId, INVITE)
                    coVerify {
                        matrixClientMock.room.joinRoom(roomId, asUserId = userId)
                    }
                }
                it("should join room when autoJoin is $ENABLED") {
                    every { botPropertiesMock.autoJoin }.returns(ENABLED)
                    cut.handleMembership(userId, roomOnForeignServerId, INVITE)
                    coVerify {
                        matrixClientMock.room.joinRoom(roomOnForeignServerId, asUserId = userId)
                    }
                }
                it("should not join and leave room when delegated server wants to") {
                    every { botPropertiesMock.autoJoin }.returns(ENABLED)
                    coEvery { membershipChangeServiceMock.shouldJoinRoom(userId, roomOnForeignServerId) }
                        .returns(false)
                    cut.handleMembership(userId, roomOnForeignServerId, INVITE)
                    coVerify {
                        matrixClientMock.room.leaveRoom(roomOnForeignServerId, asUserId = userId)
                    }
                }
            }
            describe("membership is $JOIN") {
                it("should notify service when trackMembershipMode is $ALL") {
                    every { botPropertiesMock.trackMembership }.returns(ALL)
                    every { botHelperMock.isManagedUser(userId) }.returns(false)
                    cut.handleMembership(userId, roomId, JOIN)
                    coVerify {
                        membershipChangeServiceMock.onRoomJoin(userId, roomId)
                    }
                }
                it("should notify service when trackMembershipMode is ${MatrixBotProperties.TrackMembershipMode.MANAGED} and user is managed") {
                    every { botPropertiesMock.trackMembership }.returns(MatrixBotProperties.TrackMembershipMode.MANAGED)
                    cut.handleMembership(userId, roomId, JOIN)
                    coVerify {
                        membershipChangeServiceMock.onRoomJoin(userId, roomId)
                    }
                }
                it("should not notify service when trackMembershipMode is ${MatrixBotProperties.TrackMembershipMode.MANAGED} and user is not managed") {
                    every { botPropertiesMock.trackMembership }.returns(MatrixBotProperties.TrackMembershipMode.MANAGED)
                    every { botHelperMock.isManagedUser(userId) }.returns(false)

                    cut.handleMembership(userId, roomId, JOIN)
                    coVerify {
                        membershipChangeServiceMock wasNot Called
                    }
                }
                it("should not notify service when trackMembershipMode is $NONE") {
                    every { botPropertiesMock.trackMembership }.returns(NONE)
                    cut.handleMembership(userId, roomId, JOIN)
                    coVerify {
                        membershipChangeServiceMock wasNot Called
                    }
                }
            }
            describe("membership is $LEAVE or $BAN") {
                it("should notify service when trackMembershipMode is $ALL") {
                    every { botPropertiesMock.trackMembership }.returns(ALL)
                    every { botHelperMock.isManagedUser(userId) }.returns(false)
                    cut.handleMembership(userId, roomId, LEAVE)
                    cut.handleMembership(userId, roomId, BAN)
                    coVerify(exactly = 2) {
                        membershipChangeServiceMock.onRoomLeave(userId, roomId)
                    }
                }
                it("should notify service when trackMembershipMode is ${MatrixBotProperties.TrackMembershipMode.MANAGED} and user is managed") {
                    every { botPropertiesMock.trackMembership }.returns(MatrixBotProperties.TrackMembershipMode.MANAGED)
                    cut.handleMembership(userId, roomId, LEAVE)
                    cut.handleMembership(userId, roomId, BAN)
                    coVerify(exactly = 2) {
                        membershipChangeServiceMock.onRoomLeave(userId, roomId)
                    }
                }
                it("should not notify service when trackMembershipMode is ${MatrixBotProperties.TrackMembershipMode.MANAGED} and user is not managed") {
                    every { botPropertiesMock.trackMembership }.returns(MatrixBotProperties.TrackMembershipMode.MANAGED)
                    every { botHelperMock.isManagedUser(userId) }.returns(false)

                    cut.handleMembership(userId, roomId, LEAVE)
                    cut.handleMembership(userId, roomId, BAN)
                    coVerify {
                        membershipChangeServiceMock wasNot Called
                    }
                }
                it("should not notify service when trackMembershipMode is $NONE") {
                    every { botPropertiesMock.trackMembership }.returns(NONE)
                    cut.handleMembership(userId, roomId, LEAVE)
                    cut.handleMembership(userId, roomId, BAN)

                    coVerify {
                        membershipChangeServiceMock wasNot Called
                    }
                }
            }
        }

        afterTest { clearMocks(matrixClientMock, membershipChangeServiceMock, botHelperMock) }
    }
}