package net.folivo.matrix.bot.appservice

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode.*
import net.folivo.matrix.bot.config.MatrixBotProperties.TrackMembershipMode
import net.folivo.matrix.bot.config.MatrixBotProperties.TrackMembershipMode.*
import net.folivo.matrix.bot.handler.AutoJoinService
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
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.I_AM_A_TEAPOT
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.test.StepVerifier.FirstStep

@ExtendWith(MockKExtension::class)
class MembershipEventHandlerTest {

    @MockK(relaxed = true)
    lateinit var matrixClientMock: MatrixClient

    @MockK
    lateinit var autoJoinServiceMock: AutoJoinService

    @MockK
    lateinit var roomServiceMock: DefaultMatrixAppserviceRoomService

    @MockK
    lateinit var helperMock: AppserviceHandlerHelper

    @BeforeEach
    fun setupMocks() {
        every { matrixClientMock.roomsApi.joinRoom(allAny()) } returns Mono.just("!someRoomId:someServerName")
        every { matrixClientMock.roomsApi.leaveRoom(allAny()) } returns Mono.empty()
        every { roomServiceMock.saveRoomJoin(any(), any()) } returns Mono.empty()
        every { roomServiceMock.saveRoomLeave(any(), any()) } returns Mono.empty()
        every { autoJoinServiceMock.shouldJoin(any(), any(), any()) } returns Mono.just(true)
        every { helperMock.registerAndSaveUser(any()) } returns Mono.just(true)
    }

    fun doMemberEvent(
            membership: Membership,
            userId: String,
            autoJoinMode: AutoJoinMode = ENABLED,
            roomId: String = "!someRoomId:someServerName",
            trackMembershipMode: TrackMembershipMode = NONE
    ): FirstStep<Void> {
        val cut = MembershipEventHandler(
                autoJoinService = autoJoinServiceMock,
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
        return StepVerifier
                .create(cut.handleEvent(inviteEvent, roomId))
    }

    @Test
    fun `should support MemberEvent`() {
        val cut = MembershipEventHandler(
                autoJoinService = autoJoinServiceMock,
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
        doMemberEvent(INVITE, "@someUserId:someServerName", DISABLED).verifyComplete()
        verifyAll {
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
        ).verifyComplete()
        verifyAll {
            matrixClientMock.roomsApi.leaveRoom("!someRoomId:foreignServer", "@unicorn_someUserId:someServerName")
            roomServiceMock wasNot Called
        }
    }

    @Test
    fun `should do reject invite from other server when autoJoin is restricted even with leave forbidden by homserver`() {
        every { matrixClientMock.roomsApi.leaveRoom(allAny()) } returns Mono.error(
                MatrixServerException(
                        FORBIDDEN,
                        ErrorResponse("")
                )
        )
        doMemberEvent(INVITE, "@unicorn_someUserId:someServerName", RESTRICTED, "!someRoomId:foreignServer")
                .verifyComplete()
        verifyAll {
            matrixClientMock.roomsApi.leaveRoom("!someRoomId:foreignServer", "@unicorn_someUserId:someServerName")
            roomServiceMock wasNot Called
        }
    }

    @Test
    fun `should do reject invite from other server when autoJoin is restricted and have error when not forbidden by homserver`() {
        every { matrixClientMock.roomsApi.leaveRoom(allAny()) } returns Mono.error(
                MatrixServerException(
                        I_AM_A_TEAPOT,
                        ErrorResponse("")
                )
        )
        doMemberEvent(INVITE, "@unicorn_someUserId:someServerName", RESTRICTED, "!someRoomId:foreignServer")
                .verifyError(MatrixServerException::class.java)
        verifyAll {
            matrixClientMock.roomsApi.leaveRoom("!someRoomId:foreignServer", "@unicorn_someUserId:someServerName")
            roomServiceMock wasNot Called
        }
    }

    @Test
    fun `should join invited room when autoJoin is restricted or enabled and invited user is application service user or managed`() {
        doMemberEvent(INVITE, "@someAsUsername:someServerName", RESTRICTED).verifyComplete()
        doMemberEvent(INVITE, "@someAsUsername:someServerName").verifyComplete()
        doMemberEvent(INVITE, "@unicorn_star:someServerName", RESTRICTED).verifyComplete()
        doMemberEvent(INVITE, "@unicorn_star:someServerName").verifyComplete()
        verifyOrder {
            matrixClientMock.roomsApi.joinRoom("!someRoomId:someServerName")
            matrixClientMock.roomsApi.joinRoom("!someRoomId:someServerName")
            matrixClientMock.roomsApi.joinRoom("!someRoomId:someServerName", asUserId = "@unicorn_star:someServerName")
            matrixClientMock.roomsApi.joinRoom("!someRoomId:someServerName", asUserId = "@unicorn_star:someServerName")

        }
    }

    @Test
    fun `should do reject invite when services don't want to join it`() {
        every {
            autoJoinServiceMock.shouldJoin(
                    "!someRoomId:someServerName",
                    "@someAsUsername:someServerName",
                    true
            )
        } returns Mono.just(false)
        doMemberEvent(INVITE, "@someAsUsername:someServerName").verifyComplete()
        verifyAll {
            matrixClientMock.roomsApi.leaveRoom("!someRoomId:someServerName")
            roomServiceMock wasNot Called
        }
    }

    @Test
    fun `should try to register user when join room is FORBIDDEN`() {
        every {
            matrixClientMock.roomsApi.joinRoom(
                    "!someRoomId:someServerName",
                    asUserId = "@unicorn_star:someServerName"
            )
        }.returnsMany(
                Mono.error(MatrixServerException(FORBIDDEN, ErrorResponse("FORBIDDEN"))),
                Mono.just("!someRoomId:someServerName")
        )

        doMemberEvent(INVITE, "@unicorn_star:someServerName").verifyComplete()
        verifyAll {
            helperMock.registerAndSaveUser("@unicorn_star:someServerName")
        }
        verify(exactly = 2) {
            matrixClientMock.roomsApi.joinRoom(
                    "!someRoomId:someServerName",
                    asUserId = "@unicorn_star:someServerName"
            )
        }
    }

    @Test
    fun `should try to register user when leave room is FORBIDDEN and not restricted`() {
        every {
            matrixClientMock.roomsApi.leaveRoom(
                    "!someRoomId:someServerName",
                    asUserId = "@unicorn_star:someServerName"
            )
        }.returnsMany(
                Mono.error(MatrixServerException(FORBIDDEN, ErrorResponse("FORBIDDEN"))),
                Mono.empty()
        )
        every { autoJoinServiceMock.shouldJoin(any(), any(), any()) } returns Mono.just(false)

        doMemberEvent(INVITE, "@unicorn_star:someServerName").verifyComplete()
        verify {
            helperMock.registerAndSaveUser("@unicorn_star:someServerName")
        }
        verify(exactly = 2) {
            matrixClientMock.roomsApi.leaveRoom(
                    "!someRoomId:someServerName",
                    asUserId = "@unicorn_star:someServerName"
            )
        }
    }

    @Test
    fun `should not try to register user when leave is FORBIDDEN and restricted`() {
        every {
            matrixClientMock.roomsApi.leaveRoom(
                    "!someRoomId:someOtherServerName",
                    asUserId = "@unicorn_star:someServerName"
            )
        }.returnsMany(
                Mono.error(MatrixServerException(FORBIDDEN, ErrorResponse("FORBIDDEN"))),
                Mono.empty()
        )
        every { autoJoinServiceMock.shouldJoin(any(), any(), any()) } returns Mono.just(false)

        doMemberEvent(
                INVITE,
                "@unicorn_star:someServerName",
                RESTRICTED,
                "!someRoomId:someOtherServerName"
        ).verifyComplete()
        verify(exactly = 0) { helperMock.registerAndSaveUser("@unicorn_star:someServerName") }
        verify(exactly = 1) {
            matrixClientMock.roomsApi.leaveRoom(
                    "!someRoomId:someOtherServerName",
                    asUserId = "@unicorn_star:someServerName"
            )
        }
    }

    @Test
    fun `should do nothing when invited user is not managed by application service`() {
        doMemberEvent(INVITE, "@dino_star:someServerName", RESTRICTED).verifyComplete()
        doMemberEvent(INVITE, "@dino_star:someServerName").verifyComplete()
        verifyAll {
            matrixClientMock wasNot Called
            roomServiceMock wasNot Called
        }
    }

    @Test
    fun `should save room leave when ban or leave and all membership should be tracked`() {
        doMemberEvent(BAN, "@someUser:someServerName", trackMembershipMode = ALL).verifyComplete()
        doMemberEvent(LEAVE, "@someUser:someServerName", trackMembershipMode = ALL).verifyComplete()
        verifyOrder {
            roomServiceMock.saveRoomLeave("!someRoomId:someServerName", "@someUser:someServerName")
            roomServiceMock.saveRoomLeave("!someRoomId:someServerName", "@someUser:someServerName")
        }
    }

    @Test
    fun `should not save room leave when ban or leave and only managed membership should be tracked`() {
        doMemberEvent(BAN, "@someUser:someServerName", trackMembershipMode = MANAGED).verifyComplete()
        doMemberEvent(LEAVE, "@someUser:someServerName", trackMembershipMode = MANAGED).verifyComplete()
        verify { roomServiceMock wasNot Called }
    }

    @Test
    fun `should save room leave when ban or leave and only managed membership should be tracked`() {
        doMemberEvent(BAN, "@unicorn_star:someServerName", trackMembershipMode = MANAGED).verifyComplete()
        doMemberEvent(LEAVE, "@someAsUsername:someServerName", trackMembershipMode = MANAGED).verifyComplete()
        verifyOrder {
            roomServiceMock.saveRoomLeave("!someRoomId:someServerName", "@unicorn_star:someServerName")
            roomServiceMock.saveRoomLeave("!someRoomId:someServerName", "@someAsUsername:someServerName")
        }
    }

    @Test
    fun `should not save room leave when ban or leave and no membership should be tracked`() {
        doMemberEvent(BAN, "@someAsUsername:someServerName").verifyComplete()
        doMemberEvent(LEAVE, "@unicorn_star:someServerName").verifyComplete()
        verify { roomServiceMock wasNot Called }
    }

    @Test
    fun `should save room join when ban or leave and all membership should be tracked`() {
        doMemberEvent(JOIN, "@someUser:someServerName", trackMembershipMode = ALL).verifyComplete()
        verifyOrder {
            roomServiceMock.saveRoomJoin("!someRoomId:someServerName", "@someUser:someServerName")
        }
    }

    @Test
    fun `should not save room join when ban or leave and only managed membership should be tracked`() {
        doMemberEvent(JOIN, "@someUser:someServerName", trackMembershipMode = MANAGED).verifyComplete()
        verify { roomServiceMock wasNot Called }
    }

    @Test
    fun `should save room join when ban or leave and only managed membership should be tracked`() {
        doMemberEvent(JOIN, "@unicorn_star:someServerName", trackMembershipMode = MANAGED).verifyComplete()
        doMemberEvent(JOIN, "@someAsUsername:someServerName", trackMembershipMode = MANAGED).verifyComplete()
        verifyOrder {
            roomServiceMock.saveRoomJoin("!someRoomId:someServerName", "@unicorn_star:someServerName")
            roomServiceMock.saveRoomJoin("!someRoomId:someServerName", "@someAsUsername:someServerName")
        }
    }

    @Test
    fun `should not save room join when ban or leave and no membership should be tracked`() {
        doMemberEvent(JOIN, "@someAsUsername:someServerName").verifyComplete()
        doMemberEvent(JOIN, "@unicorn_star:someServerName").verifyComplete()
        verify { roomServiceMock wasNot Called }
    }
}