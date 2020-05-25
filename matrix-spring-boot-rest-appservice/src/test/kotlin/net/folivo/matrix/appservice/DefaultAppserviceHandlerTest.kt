package net.folivo.matrix.appservice

import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import net.folivo.matrix.appservice.api.DefaultAppserviceHandler
import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService
import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService.EventProcessingState.NOT_PROCESSED
import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService.EventProcessingState.PROCESSED
import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService.RoomExistingState
import net.folivo.matrix.appservice.api.user.CreateUserParameter
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService.UserExistingState
import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.core.model.events.UnknownEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.rooms.Visibility
import net.folivo.matrix.restclient.api.user.RegisterResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockKExtension::class)
class DefaultAppserviceHandlerTest {

    @MockK
    lateinit var matrixClientMock: MatrixClient

    @MockK
    lateinit var matrixAppserviceEventServiceMock: MatrixAppserviceEventService

    @MockK
    lateinit var matrixAppserviceUserServiceMock: MatrixAppserviceUserService

    @MockK
    lateinit var matrixAppserviceRoomServiceMock: MatrixAppserviceRoomService

    @InjectMockKs
    lateinit var cut: DefaultAppserviceHandler

    @BeforeEach
    fun beforeEach() {
        every { matrixAppserviceUserServiceMock.userExistingState(allAny()) }
                .returns(Mono.just(UserExistingState.CAN_BE_CREATED))
        every { matrixAppserviceUserServiceMock.getCreateUserParameter(allAny()) }
                .returns(Mono.just(CreateUserParameter()))
        every { matrixAppserviceUserServiceMock.saveUser(allAny()) }
                .returns(Mono.empty())
        every { matrixClientMock.userApi.register(allAny()) }
                .returns(Mono.just(RegisterResponse("@someUserId:example.com")))
        every { matrixClientMock.userApi.setDisplayName(allAny()) }
                .returns(Mono.empty())
    }

    @Test
    fun `should process one event and ignore other`() {
        every { matrixAppserviceEventServiceMock.eventProcessingState("someTnxId", "someEventId1") }
                .returns(Mono.just(NOT_PROCESSED))
        every { matrixAppserviceEventServiceMock.eventProcessingState("someTnxId", "someTypeId2") }
                .returns(Mono.just(PROCESSED))
        every { matrixAppserviceEventServiceMock.processEvent(any()) }
                .returns(Mono.empty())
        every { matrixAppserviceEventServiceMock.saveEventProcessed(any(), any()) }
                .returns(Mono.empty())

        val events = arrayOf(
                mockk<MessageEvent<*>> {
                    every { id } returns "someEventId1"
                },
                mockk<UnknownEvent> {
                    every { type } returns "someTypeId2"
                }
        )

        StepVerifier
                .create(cut.addTransactions("someTnxId", Flux.fromArray(events)))
                .verifyComplete()

        verify(exactly = 1) { matrixAppserviceEventServiceMock.processEvent(events[0]) }
        verify(exactly = 1) { matrixAppserviceEventServiceMock.saveEventProcessed("someTnxId", "someEventId1") }
    }

    @Test
    fun `should not process other events on error`() {
        val event1 = mockk<MessageEvent<*>> {
            every { id } returns "someEventId1"
        }
        val event2 = mockk<UnknownEvent> {
            every { type } returns "someTypeId2"
        }

        every { matrixAppserviceEventServiceMock.eventProcessingState(any(), any()) }
                .returns(Mono.just(NOT_PROCESSED))
        every { matrixAppserviceEventServiceMock.eventProcessingState(any(), any()) }
                .returns(Mono.just(NOT_PROCESSED))
        every { matrixAppserviceEventServiceMock.processEvent(any()) }
                .returns(Mono.error(RuntimeException()))
        every { matrixAppserviceEventServiceMock.saveEventProcessed(any(), any()) }
                .returns(Mono.empty())

        StepVerifier
                .create(cut.addTransactions("someTnxId", Flux.fromArray(arrayOf(event1, event2))))
                .verifyError(RuntimeException::class.java)

        verify(exactly = 1) { matrixAppserviceEventServiceMock.processEvent(event1) }
        verify(exactly = 0) { matrixAppserviceEventServiceMock.processEvent(event2) }
        verify(exactly = 0) { matrixAppserviceEventServiceMock.saveEventProcessed("someTnxId", "someTypeId2") }
        verify(exactly = 0) { matrixAppserviceEventServiceMock.saveEventProcessed("someTnxId", "someEventId1") }
    }


    @Test
    fun `should hasUser when delegated service says it exists`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(Mono.just(UserExistingState.EXISTS))

        StepVerifier
                .create(cut.hasUser("@someUserId:example.com"))
                .assertNext { assertThat(it).isTrue() }
                .verifyComplete()
    }

    @Test
    fun `should hasUser and create it when delegated service want to`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(Mono.just(UserExistingState.CAN_BE_CREATED))
        every { matrixAppserviceUserServiceMock.getCreateUserParameter("@someUserId:example.com") }
                .returns(Mono.just(CreateUserParameter("someDisplayName")))
        every { matrixClientMock.userApi.register(allAny()) }
                .returns(Mono.just(RegisterResponse("@someUserId:example.com")))
        every { matrixClientMock.userApi.setDisplayName(allAny()) }
                .returns(Mono.empty())

        StepVerifier
                .create(cut.hasUser("@someUserId:example.com"))
                .assertNext { assertThat(it).isTrue() }
                .verifyComplete()

        verify {
            matrixClientMock.userApi.register(
                    authenticationType = "m.login.application_service",
                    username = "someUserId"
            )
        }
        verify { matrixClientMock.userApi.setDisplayName("@someUserId:example.com", "someDisplayName") }
        verify { matrixAppserviceUserServiceMock.saveUser("@someUserId:example.com") }
    }

    @Test
    fun `should not hasUser when register fails`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(Mono.just(UserExistingState.CAN_BE_CREATED))

        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.error(
                MatrixServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorResponse("500", "M_UNKNOWN")
                )
        )

        StepVerifier
                .create(cut.hasUser("@someUserId:example.com"))
                .verifyError()

        verify(exactly = 0) { matrixAppserviceUserServiceMock.saveUser(any()) }
    }

    @Test
    fun `should hasUser when saving by service fails`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(Mono.just(UserExistingState.CAN_BE_CREATED))
        every { matrixAppserviceUserServiceMock.saveUser("@someUserId:example.com") }
                .returns(Mono.error(RuntimeException()))

        every {
            matrixClientMock.userApi.setDisplayName(allAny())
        } returns Mono.empty()
        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.just(RegisterResponse("@someUserId:example.com"))

        StepVerifier
                .create(cut.hasUser("@someUserId:example.com"))
                .assertNext { assertThat(it).isTrue() }
                .verifyComplete()
    }

    @Test
    fun `hasUser should not set displayName if null`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(Mono.just(UserExistingState.CAN_BE_CREATED))
        every { matrixAppserviceUserServiceMock.getCreateUserParameter("@someUserId:example.com") }
                .returns(Mono.just(CreateUserParameter()))
        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.just(RegisterResponse("@someUserId:example.com"))

        StepVerifier
                .create(cut.hasUser("@someUserId:example.com"))
                .assertNext { assertThat(it).isTrue() }
                .verifyComplete()

        val userApi = matrixClientMock.userApi
        verify(exactly = 0) { userApi.setDisplayName(allAny()) }
    }

    @Test
    fun `should hasUser when setting displayName fails`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(Mono.just(UserExistingState.CAN_BE_CREATED))

        every {
            matrixClientMock.userApi.setDisplayName(allAny())
        } returns Mono.error(MatrixServerException(HttpStatus.BAD_REQUEST, ErrorResponse("M_UNKNOWN")))
        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.just(RegisterResponse("@someUserId:example.com"))

        StepVerifier
                .create(cut.hasUser("@someUserId:example.com"))
                .assertNext { assertThat(it).isTrue() }
                .verifyComplete()
    }

    @Test
    fun `should not hasUser when delegated service says it does not exists and should not be created`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(Mono.just(UserExistingState.DOES_NOT_EXISTS))

        StepVerifier
                .create(cut.hasUser("@someUserId:example.com"))
                .assertNext { assertThat(it).isFalse() }
                .verifyComplete()

        verify(exactly = 0) { matrixClientMock.userApi wasNot Called }
        verify(exactly = 0) { matrixAppserviceUserServiceMock.saveUser(any()) }
    }

    @Test
    fun `should hasRoomAlias when delegated service says it exists`() {
        every { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(Mono.just(RoomExistingState.EXISTS))

        StepVerifier
                .create(cut.hasRoomAlias("#someRoomAlias:example.com"))
                .assertNext { assertThat(it).isTrue() }
                .verifyComplete()
    }

    @Test
    fun `should hasRoomAlias and create it when delegated service want to`() {
        every { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(Mono.just(RoomExistingState.CAN_BE_CREATED))
        every { matrixAppserviceRoomServiceMock.getCreateRoomParameter("#someRoomAlias:example.com") }
                .returns(Mono.just(CreateRoomParameter(name = "someName")))

        every {
            matrixClientMock.roomsApi.createRoom(allAny())
        } returns Mono.just("someRoomId")

        StepVerifier
                .create(cut.hasRoomAlias("#someRoomAlias:example.com"))
                .assertNext { assertThat(it).isTrue() }
                .verifyComplete()

        verify {
            matrixClientMock.roomsApi.createRoom(
                    roomAliasName = "someRoomAlias",
                    visibility = Visibility.PUBLIC,
                    name = "someName"
            )
        }
        verify { matrixAppserviceRoomServiceMock.saveRoom("#someRoomAlias:example.com", any()) }
    }

    @Test
    fun `should not hasRoomAlias when creation fails`() {
        every { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(Mono.just(RoomExistingState.CAN_BE_CREATED))

        every {
            matrixClientMock.roomsApi.createRoom(allAny())
        } returns Mono.error(
                MatrixServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorResponse("500", "M_UNKNOWN")
                )
        )

        StepVerifier
                .create(cut.hasRoomAlias("#someRoomAlias:example.com"))
                .verifyError()

        verify(exactly = 0) { matrixAppserviceRoomServiceMock.saveRoom(any(), any()) }
    }

    @Test
    fun `should hasRoomAlias when saving by service fails`() {
        every { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(Mono.just(RoomExistingState.CAN_BE_CREATED))
        every { matrixAppserviceRoomServiceMock.getCreateRoomParameter(any()) }
                .returns(Mono.just(CreateRoomParameter()))
        every {
            matrixAppserviceRoomServiceMock.saveRoom(
                    "#someRoomAlias:example.com",
                    any()
            )
        } throws RuntimeException()

        every {
            matrixClientMock.roomsApi.createRoom(allAny())
        } returns Mono.just("someRoomId")

        StepVerifier
                .create(cut.hasRoomAlias("#someRoomAlias:example.com"))
                .assertNext { assertThat(it).isTrue() }
                .verifyComplete()

        verify { matrixAppserviceRoomServiceMock.saveRoom(any(), any()) }
    }

    @Test
    fun `should not hasRoomAlias when delegated service says it does not exists and should not be created`() {
        every { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(Mono.just(RoomExistingState.DOES_NOT_EXISTS))

        StepVerifier
                .create(cut.hasRoomAlias("#someRoomAlias:example.com"))
                .assertNext { assertThat(it).isFalse() }
                .verifyComplete()

        verify(exactly = 0) { matrixClientMock.roomsApi wasNot Called }
        verify(exactly = 0) { matrixAppserviceRoomServiceMock.saveRoom(any(), any()) }
    }
}