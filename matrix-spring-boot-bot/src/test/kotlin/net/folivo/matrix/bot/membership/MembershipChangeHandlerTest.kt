package net.folivo.matrix.bot.membership

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.*
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode.*
import net.folivo.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.*
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.*
import net.folivo.matrix.restclient.MatrixClient

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

        beforeTest {
            every { botHelperMock.getBotUserId() }.returns("@bot:server")
            every { botHelperMock.isManagedUser(any()) }.returns(true)
            every { botPropertiesMock.serverName }.returns("server")
            coEvery { membershipChangeServiceMock.shouldJoinRoom(any(), any()) }.returns(true)
        }

        describe(MembershipChangeHandler::handleMembership.name) {
            describe("membership is $INVITE") {
                it("should do nothing when autoJoin is $DISABLED") {
                    every { botPropertiesMock.autoJoin }.returns(DISABLED)
                    cut.handleMembership("@user:server", "!room:server", INVITE)
                    coVerify {
                        matrixClientMock wasNot Called
                    }
                }
                it("should do nothing when user is not managed") {
                    every { botPropertiesMock.autoJoin }.returns(ENABLED)
                    every { botHelperMock.isManagedUser("@user:server") }.returns(false)
                    cut.handleMembership("@user:server", "!room:server", INVITE)
                    coVerify {
                        matrixClientMock wasNot Called
                    }
                }
                it("should leave room when autoJoin to foreign server is $RESTRICTED") {
                    every { botPropertiesMock.autoJoin }.returns(RESTRICTED)
                    cut.handleMembership("@user:server", "!room:foreignServer", INVITE)
                    coVerify {
                        matrixClientMock.roomsApi.leaveRoom("!room:foreignServer", asUserId = "@user:server")
                    }
                }
                it("should join room when autoJoin is $RESTRICTED, but server is allowed") {
                    every { botPropertiesMock.autoJoin }.returns(RESTRICTED)
                    cut.handleMembership("@user:server", "!room:server", INVITE)
                    coVerify {
                        matrixClientMock.roomsApi.joinRoom("!room:server", asUserId = "@user:server")
                    }
                }
                it("should join room when autoJoin is $ENABLED") {
                    every { botPropertiesMock.autoJoin }.returns(ENABLED)
                    cut.handleMembership("@user:server", "!room:foreignServer", INVITE)
                    coVerify {
                        matrixClientMock.roomsApi.joinRoom("!room:foreignServer", asUserId = "@user:server")
                    }
                }
                it("should not join and leave room when delegated server wants to") {
                    every { botPropertiesMock.autoJoin }.returns(ENABLED)
                    coEvery { membershipChangeServiceMock.shouldJoinRoom("@user:server", "!room:foreignServer") }
                            .returns(false)
                    cut.handleMembership("@user:server", "!room:foreignServer", INVITE)
                    coVerify {
                        matrixClientMock.roomsApi.leaveRoom("!room:foreignServer", asUserId = "@user:server")
                    }
                }
            }
            describe("membership is $JOIN") {
                it("should notify service when trackMembershipMode is $ALL") {
                    every { botPropertiesMock.trackMembership }.returns(ALL)
                    every { botHelperMock.isManagedUser("@user:server") }.returns(false)
                    cut.handleMembership("@user:server", "!room:server", JOIN)
                    coVerify {
                        membershipChangeServiceMock.onRoomJoin("@user:server", "!room:server")
                    }
                }
                it("should notify service when trackMembershipMode is $MANAGED and user is managed") {
                    every { botPropertiesMock.trackMembership }.returns(MANAGED)
                    cut.handleMembership("@user:server", "!room:server", JOIN)
                    coVerify {
                        membershipChangeServiceMock.onRoomJoin("@user:server", "!room:server")
                    }
                }
                it("should not notify service when trackMembershipMode is $MANAGED and user is not managed") {
                    every { botPropertiesMock.trackMembership }.returns(MANAGED)
                    every { botHelperMock.isManagedUser("@user:server") }.returns(false)

                    cut.handleMembership("@user:server", "!room:server", JOIN)
                    coVerify {
                        membershipChangeServiceMock wasNot Called
                    }
                }
                it("should not notify service when trackMembershipMode is $NONE") {
                    every { botPropertiesMock.trackMembership }.returns(NONE)
                    cut.handleMembership("@user:server", "!room:server", JOIN)
                    coVerify {
                        membershipChangeServiceMock wasNot Called
                    }
                }
            }
            describe("membership is $LEAVE or $BAN") {
                it("should notify service when trackMembershipMode is $ALL") {
                    every { botPropertiesMock.trackMembership }.returns(ALL)
                    every { botHelperMock.isManagedUser("@user:server") }.returns(false)
                    cut.handleMembership("@user:server", "!room:server", LEAVE)
                    cut.handleMembership("@user:server", "!room:server", BAN)
                    coVerify(exactly = 2) {
                        membershipChangeServiceMock.onRoomLeave("@user:server", "!room:server")
                    }
                }
                it("should notify service when trackMembershipMode is $MANAGED and user is managed") {
                    every { botPropertiesMock.trackMembership }.returns(MANAGED)
                    cut.handleMembership("@user:server", "!room:server", LEAVE)
                    cut.handleMembership("@user:server", "!room:server", BAN)
                    coVerify(exactly = 2) {
                        membershipChangeServiceMock.onRoomLeave("@user:server", "!room:server")
                    }
                }
                it("should not notify service when trackMembershipMode is $MANAGED and user is not managed") {
                    every { botPropertiesMock.trackMembership }.returns(MANAGED)
                    every { botHelperMock.isManagedUser("@user:server") }.returns(false)

                    cut.handleMembership("@user:server", "!room:server", LEAVE)
                    cut.handleMembership("@user:server", "!room:server", BAN)
                    coVerify {
                        membershipChangeServiceMock wasNot Called
                    }
                }
                it("should not notify service when trackMembershipMode is $NONE") {
                    every { botPropertiesMock.trackMembership }.returns(NONE)
                    cut.handleMembership("@user:server", "!room:server", LEAVE)
                    cut.handleMembership("@user:server", "!room:server", BAN)

                    coVerify {
                        membershipChangeServiceMock wasNot Called
                    }
                }
            }
        }

        afterTest { clearMocks(matrixClientMock, membershipChangeServiceMock, botHelperMock) }
    }
}