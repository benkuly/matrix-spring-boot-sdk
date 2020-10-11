package net.folivo.matrix.bot.membership

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.bot.appservice.room.DefaultAppserviceRoomService
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode.*
import net.folivo.matrix.bot.config.MatrixBotProperties.TrackMembershipMode
import net.folivo.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.*
import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.*
import net.folivo.matrix.restclient.MatrixClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.I_AM_A_TEAPOT

@ExtendWith(MockKExtension::class)
class MembershipChangeHandlerTest {

    @MockK(relaxed = true)
    lateinit var matrixClientMock: MatrixClient

    @MockK
    lateinit var autoJoinCustomizerMock: AutoJoinCustomizer

    @MockK
    lateinit var roomServiceMock: DefaultAppserviceRoomService

    @MockK
    lateinit var helperMock: AppserviceHandlerHelper

    @BeforeEach
    fun setupMocks() {
        coEvery { matrixClientMock.roomsApi.joinRoom(allAny()) }.returns("!someRoomId:someServerName")
        coEvery { matrixClientMock.roomsApi.leaveRoom(allAny()) } just Runs
        coEvery { roomServiceMock.onRoomJoin(any(), any()) } just Runs
        coEvery { roomServiceMock.onRoomLeave(any(), any()) } just Runs
        coEvery { autoJoinCustomizerMock.shouldJoin(any(), any(), any()) }.returns(true)
        coEvery { helperMock.registerManagedUser(any()) } just Runs
    }

    private fun doMemberEvent(
            membership: Membership,
            userId: String,
            autoJoinMode: AutoJoinMode = ENABLED,
            roomId: String = "!someRoomId:someServerName",
            trackMembershipMode: TrackMembershipMode = NONE
    ) {
        val cut = MembershipChangeHandler(
                autoJoinService = autoJoinCustomizerMock,
                matrixClient = matrixClientMock,
                roomService = roomServiceMock,
                autoJoin = autoJoinMode,
                serverName = "someServerName",
                usersRegex = listOf("unicorn_.*"),
                asUsername = "someAsUsername",
                helper = helperMock,
                trackMembershipMode = trackMembershipMode
        )
        val inviteEvent = mockk<MemberEvent>(relaxed = true) {
            every { content.membership } returns membership
            every { stateKey } returns userId
        }
        runBlocking { cut.handleEvent(inviteEvent, roomId) }
    }

    @Test
    fun `should support MemberEvent`() {
        val cut = MembershipChangeHandler(
                autoJoinService = autoJoinCustomizerMock,
                matrixClient = matrixClientMock,
                roomService = roomServiceMock,
                autoJoin = DISABLED,
                serverName = "someServerName",
                usersRegex = emptyList(),
                asUsername = "someAsUsername",
                helper = helperMock,
                trackMembershipMode = NONE
        )
        assertThat(cut.supports(MemberEvent::class.java)).isTrue()
    }

    @Test
    fun `should do nothing when autoJoin is disabled`() {
        doMemberEvent(INVITE, "@someUserId:someServerName", DISABLED)
        coVerifyAll {
            matrixClientMock wasNot Called
            roomServiceMock wasNot Called
        }
    }

    @Test
    fun `should do reject invite from other server when autoJoin is restricted`() {
        doMemberEvent(
                INVITE,
                "@unicorn_someUserId:someServerName",
                RESTRICTED,
                "!someRoomId:foreignServer"
        )
        coVerifyAll {
            matrixClientMock.roomsApi.leaveRoom("!someRoomId:foreignServer", "@unicorn_someUserId:someServerName")
            roomServiceMock wasNot Called
        }
    }

    @Test
    fun `should do reject invite from other server when autoJoin is restricted even with leave forbidden by homserver`() {
        coEvery { matrixClientMock.roomsApi.leaveRoom(allAny()) }.throws(
                MatrixServerException(
                        FORBIDDEN,
                        ErrorResponse("")
                )
        )
        doMemberEvent(INVITE, "@unicorn_someUserId:someServerName", RESTRICTED, "!someRoomId:foreignServer")

        coVerifyAll {
            matrixClientMock.roomsApi.leaveRoom("!someRoomId:foreignServer", "@unicorn_someUserId:someServerName")
            roomServiceMock wasNot Called
        }
    }

    @Test
    fun `should do reject invite from other server when autoJoin is restricted and have error when not forbidden by homserver`() {
        coEvery { matrixClientMock.roomsApi.leaveRoom(allAny()) }.throws(
                MatrixServerException(
                        I_AM_A_TEAPOT,
                        ErrorResponse("")
                )
        )
        try {
            doMemberEvent(INVITE, "@unicorn_someUserId:someServerName", RESTRICTED, "!someRoomId:foreignServer")
            fail { "should have error" }
        } catch (error: MatrixServerException) {
        }
        coVerifyAll {
            matrixClientMock.roomsApi.leaveRoom("!someRoomId:foreignServer", "@unicorn_someUserId:someServerName")
            roomServiceMock wasNot Called
        }
    }

    @Test
    fun `should join invited room when autoJoin is restricted or enabled and invited user is application service user or managed`() {
        doMemberEvent(INVITE, "@someAsUsername:someServerName", RESTRICTED)
        doMemberEvent(INVITE, "@someAsUsername:someServerName")
        doMemberEvent(INVITE, "@unicorn_star:someServerName", RESTRICTED)
        doMemberEvent(INVITE, "@unicorn_star:someServerName")
        coVerifyOrder {
            matrixClientMock.roomsApi.joinRoom("!someRoomId:someServerName")
            matrixClientMock.roomsApi.joinRoom("!someRoomId:someServerName")
            matrixClientMock.roomsApi.joinRoom("!someRoomId:someServerName", asUserId = "@unicorn_star:someServerName")
            matrixClientMock.roomsApi.joinRoom("!someRoomId:someServerName", asUserId = "@unicorn_star:someServerName")

        }
    }

    @Test
    fun `should do reject invite when services don't want to join it`() {
        coEvery {
            autoJoinCustomizerMock.shouldJoin(
                    "!someRoomId:someServerName",
                    "@someAsUsername:someServerName",
                    true
            )
        }.returns(false)
        doMemberEvent(INVITE, "@someAsUsername:someServerName")
        coVerifyAll {
            matrixClientMock.roomsApi.leaveRoom("!someRoomId:someServerName")
            roomServiceMock wasNot Called
        }
    }

    @Test
    fun `should try to register user when join room is FORBIDDEN`() {
        coEvery {
            matrixClientMock.roomsApi.joinRoom(
                    "!someRoomId:someServerName",
                    asUserId = "@unicorn_star:someServerName"
            )
        }.throws(
                MatrixServerException(FORBIDDEN, ErrorResponse("FORBIDDEN"))
        ).coAndThen {
            "!someRoomId:someServerName"
        }

        doMemberEvent(INVITE, "@unicorn_star:someServerName")
        coVerifyAll {
            helperMock.registerManagedUser("@unicorn_star:someServerName")
        }
        coVerify(exactly = 2) {
            matrixClientMock.roomsApi.joinRoom(
                    "!someRoomId:someServerName",
                    asUserId = "@unicorn_star:someServerName"
            )
        }
    }

    @Test
    fun `should try to register user when leave room is FORBIDDEN and not restricted`() {
        coEvery {
            matrixClientMock.roomsApi.leaveRoom(
                    "!someRoomId:someServerName",
                    asUserId = "@unicorn_star:someServerName"
            )
        }.throws(
                MatrixServerException(FORBIDDEN, ErrorResponse("FORBIDDEN"))
        ).coAndThen { (_) -> run {} }

        coEvery { autoJoinCustomizerMock.shouldJoin(any(), any(), any()) }.returns(false)

        doMemberEvent(INVITE, "@unicorn_star:someServerName")
        coVerify {
            helperMock.registerManagedUser("@unicorn_star:someServerName")
        }
        coVerify(exactly = 2) {
            matrixClientMock.roomsApi.leaveRoom(
                    "!someRoomId:someServerName",
                    asUserId = "@unicorn_star:someServerName"
            )
        }
    }

    @Test
    fun `should not try to register user when leave is FORBIDDEN and restricted`() {
        coEvery {
            matrixClientMock.roomsApi.leaveRoom(
                    "!someRoomId:someOtherServerName",
                    asUserId = "@unicorn_star:someServerName"
            )
        }.throws(
                MatrixServerException(FORBIDDEN, ErrorResponse("FORBIDDEN"))
        )
        coEvery { autoJoinCustomizerMock.shouldJoin(any(), any(), any()) }.returns(false)

        doMemberEvent(
                INVITE,
                "@unicorn_star:someServerName",
                RESTRICTED,
                "!someRoomId:someOtherServerName"
        )
        coVerify(exactly = 0) { helperMock.registerManagedUser("@unicorn_star:someServerName") }
        coVerify(exactly = 1) {
            matrixClientMock.roomsApi.leaveRoom(
                    "!someRoomId:someOtherServerName",
                    asUserId = "@unicorn_star:someServerName"
            )
        }
    }

    @Test
    fun `should do nothing when invited user is not managed by application service`() {
        doMemberEvent(INVITE, "@dino_star:someServerName", RESTRICTED)
        doMemberEvent(INVITE, "@dino_star:someServerName")
        coVerifyAll {
            matrixClientMock wasNot Called
            roomServiceMock wasNot Called
        }
    }

    @Test
    fun `should save room leave when ban or leave and all membership should be tracked`() {
        doMemberEvent(BAN, "@someUser:someServerName", trackMembershipMode = ALL)
        doMemberEvent(LEAVE, "@someUser:someServerName", trackMembershipMode = ALL)
        coVerifyOrder {
            roomServiceMock.onRoomLeave("!someRoomId:someServerName", "@someUser:someServerName")
            roomServiceMock.onRoomLeave("!someRoomId:someServerName", "@someUser:someServerName")
        }
    }

    @Test
    fun `should not save room leave when ban or leave and only managed membership should be tracked`() {
        doMemberEvent(BAN, "@someUser:someServerName", trackMembershipMode = MANAGED)
        doMemberEvent(LEAVE, "@someUser:someServerName", trackMembershipMode = MANAGED)
        coVerify { roomServiceMock wasNot Called }
    }

    @Test
    fun `should save room leave when ban or leave and only managed membership should be tracked`() {
        doMemberEvent(BAN, "@unicorn_star:someServerName", trackMembershipMode = MANAGED)
        doMemberEvent(LEAVE, "@someAsUsername:someServerName", trackMembershipMode = MANAGED)
        coVerifyOrder {
            roomServiceMock.onRoomLeave("!someRoomId:someServerName", "@unicorn_star:someServerName")
            roomServiceMock.onRoomLeave("!someRoomId:someServerName", "@someAsUsername:someServerName")
        }
    }

    @Test
    fun `should not save room leave when ban or leave and no membership should be tracked`() {
        doMemberEvent(BAN, "@someAsUsername:someServerName")
        doMemberEvent(LEAVE, "@unicorn_star:someServerName")
        coVerify { roomServiceMock wasNot Called }
    }

    @Test
    fun `should save room join when ban or leave and all membership should be tracked`() {
        doMemberEvent(JOIN, "@someUser:someServerName", trackMembershipMode = ALL)
        coVerify {
            roomServiceMock.onRoomJoin("!someRoomId:someServerName", "@someUser:someServerName")
        }
    }

    @Test
    fun `should not save room join when ban or leave and only managed membership should be tracked`() {
        doMemberEvent(JOIN, "@someUser:someServerName", trackMembershipMode = MANAGED)
        coVerify { roomServiceMock wasNot Called }
    }

    @Test
    fun `should save room join when ban or leave and only managed membership should be tracked`() {
        doMemberEvent(JOIN, "@unicorn_star:someServerName", trackMembershipMode = MANAGED)
        doMemberEvent(JOIN, "@someAsUsername:someServerName", trackMembershipMode = MANAGED)
        coVerifyOrder {
            roomServiceMock.onRoomJoin("!someRoomId:someServerName", "@unicorn_star:someServerName")
            roomServiceMock.onRoomJoin("!someRoomId:someServerName", "@someAsUsername:someServerName")
        }
    }

    @Test
    fun `should not save room join when ban or leave and no membership should be tracked`() {
        doMemberEvent(JOIN, "@someAsUsername:someServerName")
        doMemberEvent(JOIN, "@unicorn_star:someServerName")
        coVerify { roomServiceMock wasNot Called }
    }
}