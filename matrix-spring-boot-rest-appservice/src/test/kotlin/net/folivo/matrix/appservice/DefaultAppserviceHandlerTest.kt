package net.folivo.matrix.appservice

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
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
import org.junit.jupiter.api.fail

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
        coEvery { matrixAppserviceUserServiceMock.userExistingState(allAny()) }
                .returns(UserExistingState.CAN_BE_CREATED)
        coEvery { matrixAppserviceUserServiceMock.getCreateUserParameter(allAny()) }
                .returns(CreateUserParameter())
        coEvery { matrixAppserviceUserServiceMock.saveUser(allAny()) } just Runs
        coEvery { helperMock.registerAndSaveUser(any()) } just Runs
        coEvery { helperMock.createAndSaveRoom(any()) } just Runs
    }

    @Test
    fun `should process one event and ignore other`() {
        coEvery { matrixAppserviceEventServiceMock.eventProcessingState("someTnxId", "someEventId1") }
                .returns(NOT_PROCESSED)
        coEvery { matrixAppserviceEventServiceMock.eventProcessingState("someTnxId", "someTypeId2") }
                .returns(PROCESSED)
        coEvery { matrixAppserviceEventServiceMock.processEvent(any()) } just Runs
        coEvery { matrixAppserviceEventServiceMock.saveEventProcessed(any(), any()) } just Runs

        val events = arrayOf(
                mockk<MessageEvent<*>> {
                    every { id } returns "someEventId1"
                },
                mockk<UnknownEvent> {
                    every { type } returns "someTypeId2"
                }
        )

        runBlocking { cut.addTransactions("someTnxId", flowOf(*events)) }

        coVerify(exactly = 1) { matrixAppserviceEventServiceMock.processEvent(events[0]) }
        coVerify(exactly = 1) { matrixAppserviceEventServiceMock.saveEventProcessed("someTnxId", "someEventId1") }
    }

    @Test
    fun `should not process other events on error`() {
        val event1 = mockk<MessageEvent<*>> {
            every { id } returns "someEventId1"
        }
        val event2 = mockk<UnknownEvent> {
            every { type } returns "someTypeId2"
        }

        coEvery { matrixAppserviceEventServiceMock.eventProcessingState(any(), any()) }
                .returns(NOT_PROCESSED)
        coEvery { matrixAppserviceEventServiceMock.eventProcessingState(any(), any()) }
                .returns(NOT_PROCESSED)
        coEvery { matrixAppserviceEventServiceMock.processEvent(any()) }
                .throws(RuntimeException())
        coEvery { matrixAppserviceEventServiceMock.saveEventProcessed(any(), any()) } just Runs

        try {
            runBlocking { cut.addTransactions("someTnxId", flowOf(event1, event2)) }
            fail { "should have error" }
        } catch (error: Throwable) {
        }

        coVerify(exactly = 1) { matrixAppserviceEventServiceMock.processEvent(event1) }
        coVerify(exactly = 0) { matrixAppserviceEventServiceMock.processEvent(event2) }
        coVerify(exactly = 0) { matrixAppserviceEventServiceMock.saveEventProcessed("someTnxId", "someTypeId2") }
        coVerify(exactly = 0) { matrixAppserviceEventServiceMock.saveEventProcessed("someTnxId", "someEventId1") }
    }


    @Test
    fun `should hasUser when delegated service says it exists`() {
        coEvery { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(UserExistingState.EXISTS)

        val hasUser = runBlocking { cut.hasUser("@someUserId:example.com") }
        assertThat(hasUser).isTrue()
    }

    @Test
    fun `should hasUser and create it when delegated service want to`() {
        coEvery { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(UserExistingState.CAN_BE_CREATED)

        val hasUser = runBlocking { cut.hasUser("@someUserId:example.com") }
        assertThat(hasUser).isTrue()

        coVerify { helperMock.registerAndSaveUser("@someUserId:example.com") }
    }

    @Test
    fun `should have error when helper fails`() {
        coEvery { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(UserExistingState.CAN_BE_CREATED)
        coEvery { helperMock.registerAndSaveUser(any()) }
                .throws(RuntimeException())

        try {
            runBlocking { cut.hasUser("@someUserId:example.com") }
            fail { "should have error" }
        } catch (error: Throwable) {

        }
    }

    @Test
    fun `should not hasUser when delegated service says it does not exists and should not be created`() {
        coEvery { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(UserExistingState.DOES_NOT_EXISTS)

        val hasUser = runBlocking { cut.hasUser("@someUserId:example.com") }
        assertThat(hasUser).isFalse()

        coVerify(exactly = 0) { helperMock wasNot Called }
    }

    @Test
    fun `should hasRoomAlias when delegated service says it exists`() {
        coEvery { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(RoomExistingState.EXISTS)

        val hasRoom = runBlocking { cut.hasRoomAlias("#someRoomAlias:example.com") }
        assertThat(hasRoom).isTrue()
    }

    @Test
    fun `should hasRoomAlias and create it when delegated service want to`() {
        coEvery { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(RoomExistingState.CAN_BE_CREATED)
        coEvery { matrixAppserviceRoomServiceMock.getCreateRoomParameter("#someRoomAlias:example.com") }
                .returns(CreateRoomParameter(name = "someName"))

        val hasRoom = runBlocking { cut.hasRoomAlias("#someRoomAlias:example.com") }
        assertThat(hasRoom).isTrue()

        coVerify { helperMock.createAndSaveRoom("#someRoomAlias:example.com") }
    }

    @Test
    fun `should not hasRoomAlias when creation fails`() {
        coEvery { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(RoomExistingState.CAN_BE_CREATED)

        coEvery { helperMock.createAndSaveRoom(any()) }.throws(RuntimeException())

        try {
            runBlocking { cut.hasRoomAlias("#someRoomAlias:example.com") }
            fail { "should have error" }
        } catch (error: Throwable) {

        }
    }

    @Test
    fun `should not hasRoomAlias when delegated service says it does not exists and should not be created`() {
        coEvery { matrixAppserviceRoomServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(RoomExistingState.DOES_NOT_EXISTS)

        val hasRoom = runBlocking { cut.hasRoomAlias("#someRoomAlias:example.com") }
        assertThat(hasRoom).isFalse()

        coVerify(exactly = 0) { helperMock wasNot Called }
    }
}