package net.folivo.matrix.appservice

import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
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
import net.folivo.matrix.core.model.events.UnknownEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockKExtension::class)
class DefaultAppserviceHandlerTest {

    @MockK
    lateinit var matrixAppserviceEventServiceMock: MatrixAppserviceEventService

    @MockK
    lateinit var matrixAppserviceUserServiceMock: MatrixAppserviceUserService

    @MockK
    lateinit var matrixAppserviceRoomServiceMock: MatrixAppserviceRoomService

    @MockK
    lateinit var helperMock: AppserviceHandlerHelper

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
        every { helperMock.registerAndSaveUser(any()) }
                .returns(Mono.just(true))
        every { helperMock.createAndSaveRoom(any()) }
                .returns(Mono.just(true))
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
                .verifyError()

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

        StepVerifier
                .create(cut.hasUser("@someUserId:example.com"))
                .assertNext { assertThat(it).isTrue() }
                .verifyComplete()

        verify { helperMock.registerAndSaveUser("@someUserId:example.com") }
    }

    @Test
    fun `should has error when helper fails`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(Mono.just(UserExistingState.CAN_BE_CREATED))
        every { helperMock.registerAndSaveUser(any()) }
                .returns(Mono.error(RuntimeException()))

        StepVerifier
                .create(cut.hasUser("@someUserId:example.com"))
                .verifyError()
    }

    @Test
    fun `should not hasUser when delegated service says it does not exists and should not be created`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(Mono.just(UserExistingState.DOES_NOT_EXISTS))

        StepVerifier
                .create(cut.hasUser("@someUserId:example.com"))
                .assertNext { assertThat(it).isFalse() }
                .verifyComplete()

        verify(exactly = 0) { helperMock wasNot Called }
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

        StepVerifier
                .create(cut.hasRoomAlias("#someRoomAlias:example.com"))
                .assertNext { assertThat(it).isTrue() }
                .verifyComplete()

        verify { helperMock.createAndSaveRoom("#someRoomAlias:example.com") }
    }

    @Test
    fun `should not hasRoomAlias when creation fails`() {
        every { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(Mono.just(RoomExistingState.CAN_BE_CREATED))

        every { helperMock.createAndSaveRoom(any()) } returns Mono.error(RuntimeException())

        StepVerifier
                .create(cut.hasRoomAlias("#someRoomAlias:example.com"))
                .verifyError()
    }

    @Test
    fun `should not hasRoomAlias when delegated service says it does not exists and should not be created`() {
        every { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(Mono.just(RoomExistingState.DOES_NOT_EXISTS))

        StepVerifier
                .create(cut.hasRoomAlias("#someRoomAlias:example.com"))
                .assertNext { assertThat(it).isFalse() }
                .verifyComplete()

        verify(exactly = 0) { helperMock wasNot Called }
    }
}