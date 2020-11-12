package net.folivo.matrix.appservice

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.appservice.api.DefaultAppserviceHandler
import net.folivo.matrix.appservice.api.event.AppserviceEventService
import net.folivo.matrix.appservice.api.event.AppserviceEventService.EventProcessingState.NOT_PROCESSED
import net.folivo.matrix.appservice.api.event.AppserviceEventService.EventProcessingState.PROCESSED
import net.folivo.matrix.appservice.api.room.AppserviceRoomService
import net.folivo.matrix.appservice.api.room.AppserviceRoomService.RoomExistingState
import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.user.AppserviceUserService
import net.folivo.matrix.appservice.api.user.AppserviceUserService.UserExistingState
import net.folivo.matrix.appservice.api.user.RegisterUserParameter
import net.folivo.matrix.core.model.MatrixId.*
import net.folivo.matrix.core.model.events.UnknownEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail

@ExtendWith(MockKExtension::class)
class DefaultAppserviceHandlerTest {

    @MockK
    lateinit var appserviceEventServiceMock: AppserviceEventService

    @MockK
    lateinit var appserviceUserServiceMock: AppserviceUserService

    @MockK
    lateinit var appserviceRoomServiceMock: AppserviceRoomService

    @MockK
    lateinit var helperMock: AppserviceHandlerHelper

    @InjectMockKs
    lateinit var cut: DefaultAppserviceHandler

    @BeforeEach
    fun beforeEach() {
        coEvery { appserviceUserServiceMock.userExistingState(allAny()) }
                .returns(UserExistingState.CAN_BE_CREATED)
        coEvery { appserviceUserServiceMock.getRegisterUserParameter(allAny()) }
                .returns(RegisterUserParameter())
        coEvery { appserviceUserServiceMock.onRegisteredUser(allAny()) } just Runs
        coEvery { helperMock.registerManagedUser(any()) } just Runs
        coEvery { helperMock.createManagedRoom(any()) } just Runs
    }

    @Test
    fun `should process one event and ignore other`() {
        coEvery { appserviceEventServiceMock.eventProcessingState("someTnxId", EventId("event1", "server")) }
                .returns(NOT_PROCESSED)
        coEvery { appserviceEventServiceMock.eventProcessingState("someTnxId", EventId("event2", "server")) }
                .returns(PROCESSED)
        coEvery { appserviceEventServiceMock.processEvent(any()) } just Runs
        coEvery { appserviceEventServiceMock.onEventProcessed(any(), any()) } just Runs

        val events = arrayOf(
                mockk<MessageEvent<*>> {
                    every { id } returns EventId("event1", "server")
                },
                mockk<MessageEvent<*>> {
                    every { id } returns EventId("event2", "server")
                }
        )

        runBlocking { cut.addTransactions("someTnxId", flowOf(*events)) }

        coVerify(exactly = 1) { appserviceEventServiceMock.processEvent(events[0]) }
        coVerify(exactly = 1) { appserviceEventServiceMock.onEventProcessed("someTnxId", EventId("event1", "server")) }
    }

    @Test
    fun `should process event without id`() {
        coEvery { appserviceEventServiceMock.processEvent(any()) } just Runs
        coEvery { appserviceEventServiceMock.onEventProcessed(any(), any()) } just Runs

        val events = arrayOf(
                mockk<UnknownEvent> {
                    every { type } returns "someTypeId2"
                }
        )

        runBlocking { cut.addTransactions("someTnxId", flowOf(*events)) }

        coVerify(exactly = 1) { appserviceEventServiceMock.processEvent(events[0]) }
        coVerify(exactly = 0) { appserviceEventServiceMock.eventProcessingState(any(), any()) }
        coVerify(exactly = 0) { appserviceEventServiceMock.onEventProcessed(any(), any()) }
    }

    @Test
    fun `should not process other events on error`() {
        val event1 = mockk<MessageEvent<*>> {
            every { id } returns EventId("event1", "server")
        }
        val event2 = mockk<MessageEvent<*>> {
            every { id } returns EventId("event2", "server")
        }

        coEvery { appserviceEventServiceMock.eventProcessingState(any(), any()) }
                .returns(NOT_PROCESSED)
        coEvery { appserviceEventServiceMock.processEvent(any()) }
                .throws(RuntimeException())
        coEvery { appserviceEventServiceMock.onEventProcessed(any(), any()) } just Runs

        try {
            runBlocking { cut.addTransactions("someTnxId", flowOf(event1, event2)) }
            fail { "should have error" }
        } catch (error: Throwable) {
        }

        coVerify(exactly = 1) { appserviceEventServiceMock.processEvent(event1) }
        coVerify(exactly = 0) { appserviceEventServiceMock.processEvent(event2) }
        coVerify(exactly = 0) { appserviceEventServiceMock.onEventProcessed("someTnxId", any()) }
    }


    @Test
    fun `should hasUser when delegated service says it exists`() {
        coEvery { appserviceUserServiceMock.userExistingState(UserId("user", "server")) }
                .returns(UserExistingState.EXISTS)

        val hasUser = runBlocking { cut.hasUser(UserId("user", "server")) }
        assertThat(hasUser).isTrue()
    }

    @Test
    fun `should hasUser and create it when delegated service want to`() {
        coEvery { appserviceUserServiceMock.userExistingState(UserId("user", "server")) }
                .returns(UserExistingState.CAN_BE_CREATED)

        val hasUser = runBlocking { cut.hasUser(UserId("user", "server")) }
        assertThat(hasUser).isTrue()

        coVerify { helperMock.registerManagedUser(UserId("user", "server")) }
    }

    @Test
    fun `should have error when helper fails`() {
        coEvery { appserviceUserServiceMock.userExistingState(UserId("user", "server")) }
                .returns(UserExistingState.CAN_BE_CREATED)
        coEvery { helperMock.registerManagedUser(any()) }
                .throws(RuntimeException())

        try {
            runBlocking { cut.hasUser(UserId("user", "server")) }
            fail { "should have error" }
        } catch (error: Throwable) {

        }
    }

    @Test
    fun `should not hasUser when delegated service says it does not exists and should not be created`() {
        coEvery { appserviceUserServiceMock.userExistingState(UserId("user", "server")) }
                .returns(UserExistingState.DOES_NOT_EXISTS)

        val hasUser = runBlocking { cut.hasUser(UserId("user", "server")) }
        assertThat(hasUser).isFalse()

        coVerify(exactly = 0) { helperMock wasNot Called }
    }

    @Test
    fun `should hasRoomAlias when delegated service says it exists`() {
        coEvery { appserviceRoomServiceMock.roomExistingState(RoomAliasId("alias", "server")) }
                .returns(RoomExistingState.EXISTS)

        val hasRoom = runBlocking { cut.hasRoomAlias(RoomAliasId("alias", "server")) }
        assertThat(hasRoom).isTrue()
    }

    @Test
    fun `should hasRoomAlias and create it when delegated service want to`() {
        coEvery { appserviceRoomServiceMock.roomExistingState(RoomAliasId("alias", "server")) }
                .returns(RoomExistingState.CAN_BE_CREATED)
        coEvery { appserviceRoomServiceMock.getCreateRoomParameter(RoomAliasId("alias", "server")) }
                .returns(CreateRoomParameter(name = "someName"))

        val hasRoom = runBlocking { cut.hasRoomAlias(RoomAliasId("alias", "server")) }
        assertThat(hasRoom).isTrue()

        coVerify { helperMock.createManagedRoom(RoomAliasId("alias", "server")) }
    }

    @Test
    fun `should not hasRoomAlias when creation fails`() {
        coEvery { appserviceRoomServiceMock.roomExistingState(RoomAliasId("alias", "server")) }
                .returns(RoomExistingState.CAN_BE_CREATED)

        coEvery { helperMock.createManagedRoom(any()) }.throws(RuntimeException())

        try {
            runBlocking { cut.hasRoomAlias(RoomAliasId("alias", "server")) }
            fail { "should have error" }
        } catch (error: Throwable) {

        }
    }

    @Test
    fun `should not hasRoomAlias when delegated service says it does not exists and should not be created`() {
        coEvery { appserviceRoomServiceMock.roomExistingState(RoomAliasId("alias", "server")) }
                .returns(RoomExistingState.DOES_NOT_EXISTS)

        val hasRoom = runBlocking { cut.hasRoomAlias(RoomAliasId("alias", "server")) }
        assertThat(hasRoom).isFalse()

        coVerify(exactly = 0) { helperMock wasNot Called }
    }
}