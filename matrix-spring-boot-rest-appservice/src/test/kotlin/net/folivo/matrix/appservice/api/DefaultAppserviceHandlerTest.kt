package net.folivo.matrix.appservice.api

import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService
import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.appservice.api.user.CreateUserParameter
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.core.model.events.UnknownEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.rooms.Visibility
import net.folivo.matrix.restclient.api.user.RegisterResponse
import org.assertj.core.api.Assertions.assertThat
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

    @MockK(relaxed = true)
    lateinit var matrixAppserviceEventServiceMock: MatrixAppserviceEventService

    @MockK(relaxed = true)
    lateinit var matrixAppserviceUserServiceMock: MatrixAppserviceUserService

    @MockK(relaxed = true)
    lateinit var matrixAppserviceRoomServiceMock: MatrixAppserviceRoomService

    @InjectMockKs
    lateinit var cut: DefaultAppserviceHandler

    @Test
    fun `should process one event and ignore other`() {
        every { matrixAppserviceEventServiceMock.eventProcessingState("someTnxId", "someEventId1") }
                .returns(MatrixAppserviceEventService.EventProcessingState.NOT_PROCESSED)
        every { matrixAppserviceEventServiceMock.eventProcessingState("someTnxId", "someTypeId2") }
                .returns(MatrixAppserviceEventService.EventProcessingState.PROCESSED)

        val events = arrayOf(
                mockk<MessageEvent<*>> {
                    every { id } returns "someEventId1"
                },
                mockk<UnknownEvent> {
                    every { type } returns "someTypeId2"
                }
        )

        val result = cut.addTransactions("someTnxId", Flux.fromArray(events))

        StepVerifier.create(result).verifyComplete()

        verify(exactly = 1) { matrixAppserviceEventServiceMock.processEvent(events[0]) }
        verify(exactly = 1) { matrixAppserviceEventServiceMock.saveEventProcessed("someTnxId", "someEventId1") }
        verify { matrixAppserviceEventServiceMock.eventProcessingState("someTnxId", "someEventId1") }
        verify { matrixAppserviceEventServiceMock.eventProcessingState("someTnxId", "someTypeId2") }
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
                .returns(MatrixAppserviceEventService.EventProcessingState.NOT_PROCESSED)
        every { matrixAppserviceEventServiceMock.eventProcessingState(any(), any()) }
                .returns(MatrixAppserviceEventService.EventProcessingState.NOT_PROCESSED)
        every { matrixAppserviceEventServiceMock.processEvent(event1) } throws RuntimeException()

        val result = cut.addTransactions("someTnxId", Flux.fromArray(arrayOf(event1, event2)))

        StepVerifier.create(result).verifyError(RuntimeException::class.java)

        verify(exactly = 1) { matrixAppserviceEventServiceMock.processEvent(event1) }
        verify(exactly = 0) { matrixAppserviceEventServiceMock.processEvent(event2) }
        verify(exactly = 0) { matrixAppserviceEventServiceMock.saveEventProcessed("someTnxId", "someTypeId2") }
        verify(exactly = 0) { matrixAppserviceEventServiceMock.saveEventProcessed("someTnxId", "someEventId1") }
    }


    @Test
    fun `should hasUser when delegated service says it exists`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(MatrixAppserviceUserService.UserExistingState.EXISTS)

        val result = cut.hasUser("@someUserId:example.com").block()
        assertThat(result).isTrue()
    }

    @Test
    fun `should hasUser and create it when delegated service want to`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(MatrixAppserviceUserService.UserExistingState.CAN_BE_CREATED)
        every { matrixAppserviceUserServiceMock.getCreateUserParameter("@someUserId:example.com") }
                .returns(CreateUserParameter("someDisplayName"))
        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.just(RegisterResponse("@someUserId:example.com"))
        every {
            matrixClientMock.userApi.setDisplayName(allAny())
        } returns Mono.empty()

        val result = cut.hasUser("@someUserId:example.com").block()
        assertThat(result).isTrue()

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
                .returns(MatrixAppserviceUserService.UserExistingState.CAN_BE_CREATED)

        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.error(
                MatrixServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorResponse("500", "M_UNKNOWN")
                )
        )

        val result = cut.hasUser("@someUserId:example.com")
        StepVerifier.create(result)
                .verifyError()

        verify(exactly = 0) { matrixAppserviceUserServiceMock.saveUser(any()) }
    }

    @Test
    fun `should hasUser when saving by service fails`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(MatrixAppserviceUserService.UserExistingState.CAN_BE_CREATED)
        every { matrixAppserviceUserServiceMock.saveUser("@someUserId:example.com") } throws RuntimeException()

        every {
            matrixClientMock.userApi.setDisplayName(allAny())
        } returns Mono.empty()
        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.just(RegisterResponse("@someUserId:example.com"))

        val result = cut.hasUser("@someUserId:example.com").block()
        assertThat(result).isTrue()
    }

    @Test
    fun `should hasUser when setting displayName fails`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(MatrixAppserviceUserService.UserExistingState.CAN_BE_CREATED)

        every {
            matrixClientMock.userApi.setDisplayName(allAny())
        } returns Mono.error(MatrixServerException(HttpStatus.BAD_REQUEST, ErrorResponse("M_UNKNOWN")))
        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.just(RegisterResponse("@someUserId:example.com"))

        val result = cut.hasUser("@someUserId:example.com").block()
        assertThat(result).isTrue()
    }

    @Test
    fun `should not hasUser when delegated service says it does not exists and should not be created`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(MatrixAppserviceUserService.UserExistingState.DOES_NOT_EXISTS)

        val result = cut.hasUser("@someUserId:example.com").block()
        assertThat(result).isFalse()

        verify(exactly = 0) { matrixClientMock.userApi wasNot Called }
        verify(exactly = 0) { matrixAppserviceUserServiceMock.saveUser(any()) }
    }

    @Test
    fun `should hasRoomAlias when delegated service says it exists`() {
        every { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(MatrixAppserviceRoomService.RoomExistingState.EXISTS)

        val result = cut.hasRoomAlias("#someRoomAlias:example.com").block()
        assertThat(result).isTrue()
    }

    @Test
    fun `should hasRoomAlias and create it when delegated service want to`() {
        every { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(MatrixAppserviceRoomService.RoomExistingState.CAN_BE_CREATED)
        every { matrixAppserviceRoomServiceMock.getCreateRoomParameter("#someRoomAlias:example.com") }
                .returns(CreateRoomParameter(name = "someName"))

        every {
            matrixClientMock.roomsApi.createRoom(allAny())
        } returns Mono.just("someRoomId")

        val result = cut.hasRoomAlias("#someRoomAlias:example.com").block()
        assertThat(result).isTrue()

        verify {
            matrixClientMock.roomsApi.createRoom(
                    roomAliasName = "someRoomAlias",
                    visibility = Visibility.PUBLIC,
                    name = "someName"
            )
        }
        verify { matrixAppserviceRoomServiceMock.saveRoom("#someRoomAlias:example.com", "someRoomId") }
    }

    @Test
    fun `should not hasRoomAlias when creation fails`() {
        every { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(MatrixAppserviceRoomService.RoomExistingState.CAN_BE_CREATED)

        every {
            matrixClientMock.roomsApi.createRoom(allAny())
        } returns Mono.error(
                MatrixServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorResponse("500", "M_UNKNOWN")
                )
        )

        val result = cut.hasRoomAlias("#someRoomAlias:example.com")
        StepVerifier.create(result)
                .verifyError()

        verify(exactly = 0) { matrixAppserviceRoomServiceMock.saveRoom(any(), any()) }
    }

    @Test
    fun `should hasRoomAlias when saving by service fails`() {
        every { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(MatrixAppserviceRoomService.RoomExistingState.CAN_BE_CREATED)
        every {
            matrixAppserviceRoomServiceMock.saveRoom(
                    "#someRoomAlias:example.com",
                    "someRoomId"
            )
        } throws RuntimeException()

        every {
            matrixClientMock.roomsApi.createRoom(allAny())
        } returns Mono.just("someRoomId")

        val result = cut.hasRoomAlias("#someRoomAlias:example.com").block()
        assertThat(result).isTrue()

        verify { matrixAppserviceRoomServiceMock.saveRoom(any(), any()) }
    }

    @Test
    fun `should not hasRoomAlias when delegated service says it does not exists and should not be created`() {
        every { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(MatrixAppserviceRoomService.RoomExistingState.DOES_NOT_EXISTS)

        val result = cut.hasRoomAlias("#someRoomAlias:example.com").block()
        assertThat(result).isFalse()

        verify(exactly = 0) { matrixClientMock.roomsApi wasNot Called }
        verify(exactly = 0) { matrixAppserviceRoomServiceMock.saveRoom(any(), any()) }
    }
}