package net.folivo.matrix.bot.client

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode.DISABLED
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode.RESTRICTED
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.bot.membership.AutoJoinCustomizer
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.sync.SyncResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MatrixClientBotTest {

    @RelaxedMockK
    lateinit var matrixClientMock: MatrixClient

    @RelaxedMockK
    lateinit var eventHandlerMock1: MatrixEventHandler

    @RelaxedMockK
    lateinit var eventHandlerMock2: MatrixEventHandler

    @MockK
    lateinit var autoJoinCustomizerMock: AutoJoinCustomizer

    @BeforeEach
    fun beforeEach() {
        coEvery { autoJoinCustomizerMock.shouldJoin(any(), any(), any()) }.returns(true)
    }

    @Test
    fun `should start and call handler`() {
        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(
                        eventHandlerMock1,
                        eventHandlerMock2
                ),
                MatrixBotProperties(serverName = "someServerName"),
                autoJoinCustomizerMock
        )

        val event1 = mockk<MessageEvent<TextMessageEventContent>>()
        val event2 = mockk<MessageEvent<TextMessageEventContent>>()
        val event3 = mockk<MessageEvent<TextMessageEventContent>>()

        val response1 = mockk<SyncResponse>(relaxed = true) {
            every { room.join } returns mapOf(
                    "someRoomId1" to mockk(relaxed = true) {
                        every { timeline.events } returns listOf(
                                event1,
                                event2
                        )
                    }
            )
        }
        val response2 = mockk<SyncResponse>(relaxed = true) {
            every { room.join } returns mapOf(
                    "someRoomId2" to mockk(relaxed = true) {
                        every { timeline.events } returns listOf(
                                event3
                        )
                    }
            )
        }

        every { eventHandlerMock1.supports(any()) } returns true
        every { eventHandlerMock1.supports(any()) } returns false
        every { eventHandlerMock1.supports(any()) } returns true
        every { eventHandlerMock2.supports(any()) } returns true

        every { matrixClientMock.syncApi.syncLoop() }.returns(flowOf(response1, response2))

        runBlocking { cut.start().join() }

        coVerifyOrder {
            eventHandlerMock1.handleEvent(event1, "someRoomId1")
            eventHandlerMock1.handleEvent(event3, "someRoomId2")
        }
        coVerifyOrder {
            eventHandlerMock2.handleEvent(event1, "someRoomId1")
            eventHandlerMock2.handleEvent(event2, "someRoomId1")
            eventHandlerMock2.handleEvent(event3, "someRoomId2")
        }
    }

    @Test
    fun `should join rooms`() {
        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(),
                MatrixBotProperties(autoJoin = AutoJoinMode.ENABLED, serverName = "someServerName"),
                autoJoinCustomizerMock
        )

        val response1 = mockk<SyncResponse>(relaxed = true) {
            every { room.invite } returns mapOf(
                    "someRoomId1" to mockk(relaxed = true),
                    "someRoomId2" to mockk(relaxed = true)
            )
        }

        every { matrixClientMock.syncApi.syncLoop() }.returns(flowOf(response1))

        runBlocking { cut.start().join() }

        val roomsApiMock = matrixClientMock.roomsApi

        coVerifyOrder {
            roomsApiMock.joinRoom("someRoomId1")
            roomsApiMock.joinRoom("someRoomId2")
        }
        coVerify(exactly = 0) { roomsApiMock.leaveRoom(any()) }
    }

    @Test
    fun `should not join rooms`() {
        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(),
                MatrixBotProperties(autoJoin = DISABLED, serverName = "someServerName"),
                autoJoinCustomizerMock
        )

        val response1 = mockk<SyncResponse>(relaxed = true) {
            every { room.invite } returns mapOf(
                    "someRoomId1" to mockk(relaxed = true)
            )
        }

        every { matrixClientMock.syncApi.syncLoop() }.returns(flowOf(response1))

        runBlocking { cut.start().join() }

        val roomsApiMock = matrixClientMock.roomsApi

        coVerify(exactly = 0) { roomsApiMock.joinRoom(any()) }
        coVerify { roomsApiMock.leaveRoom("someRoomId1") }
    }

    @Test
    fun `should not join from foreign servers`() {
        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(),
                MatrixBotProperties(autoJoin = RESTRICTED, serverName = "someServerName"),
                autoJoinCustomizerMock
        )

        val response1 = mockk<SyncResponse>(relaxed = true) {
            every { room.invite } returns mapOf(
                    "!someRoomId1:someOtherServer" to mockk(relaxed = true),
                    "!someRoomId2:someServerName" to mockk(relaxed = true)
            )
        }

        every { matrixClientMock.syncApi.syncLoop() }.returns(flowOf(response1))

        runBlocking { cut.start().join() }

        val roomsApiMock = matrixClientMock.roomsApi

        coVerify(exactly = 1) {
            roomsApiMock.joinRoom("!someRoomId2:someServerName")
            roomsApiMock.leaveRoom("!someRoomId1:someOtherServer")
        }
    }

    @Test
    fun `should not join room when service don't want it`() {
        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(),
                MatrixBotProperties(autoJoin = DISABLED, serverName = "someServerName"),
                autoJoinCustomizerMock
        )

        val response1 = mockk<SyncResponse>(relaxed = true) {
            every { room.invite } returns mapOf(
                    "someRoomId1" to mockk(relaxed = true)
            )
        }
        coEvery { autoJoinCustomizerMock.shouldJoin("someRoomId", any(), any()) }.returns(false)

        every { matrixClientMock.syncApi.syncLoop() }.returns(flowOf(response1))

        runBlocking { cut.start().join() }

        val roomsApiMock = matrixClientMock.roomsApi

        coVerify(exactly = 0) { roomsApiMock.joinRoom(any()) }
        coVerify { roomsApiMock.leaveRoom("someRoomId1") }
    }

    @Test
    fun `should deal with multiple starts`() {
        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(eventHandlerMock1),
                MatrixBotProperties(serverName = "someServerName"),
                autoJoinCustomizerMock
        )

        val response = mockk<SyncResponse>(relaxed = true) {
            every { room.join } returns mapOf(
                    "roomId" to mockk(relaxed = true) {
                        every { timeline.events } returns listOf(
                                mockk<MessageEvent<TextMessageEventContent>>()
                        )
                    }
            )
        }

        every { eventHandlerMock1.supports(any()) } returns true

        every { matrixClientMock.syncApi.syncLoop() }.returns(flowOf(response))

        runBlocking { cut.start().join() }
        runBlocking { cut.start().join() }

        coVerify(exactly = 2) { eventHandlerMock1.handleEvent(any(), any()) }
    }

    @Test
    fun `should stop`() {
        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(eventHandlerMock1),
                MatrixBotProperties(serverName = "someServerName"),
                autoJoinCustomizerMock
        )

        val response = mockk<SyncResponse>(relaxed = true) {
            every { room.join } returns mapOf(
                    "roomId" to mockk(relaxed = true) {
                        every { timeline.events } returns listOf(
                                mockk<MessageEvent<TextMessageEventContent>>()
                        )
                    }
            )
        }

        every { eventHandlerMock1.supports(any()) } returns true

        every { matrixClientMock.syncApi.syncLoop() }.returns(flowOf(response))

        runBlocking { cut.start().join() }
        cut.stop()

        coVerify(exactly = 1) { eventHandlerMock1.handleEvent(any(), any()) }
    }

    @Test
    fun `should ignore errors`() {
        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(
                        eventHandlerMock1,
                        eventHandlerMock2
                ),
                MatrixBotProperties(serverName = "someServerName"),
                autoJoinCustomizerMock
        )

        val event1 = mockk<MessageEvent<TextMessageEventContent>>()
        val event2 = mockk<MessageEvent<TextMessageEventContent>>()

        val response1 = mockk<SyncResponse>(relaxed = true) {
            every { room.join } returns mapOf(
                    "someRoomId1" to mockk(relaxed = true) {
                        every { timeline.events } returns listOf(
                                event1,
                                event2
                        )
                    }
            )
        }

        every { eventHandlerMock1.supports(any()) } returns true
        every { eventHandlerMock2.supports(any()) } returns true
        coEvery { eventHandlerMock1.handleEvent(any()) }.throws(RuntimeException())

        every { matrixClientMock.syncApi.syncLoop() }.returns(flowOf(response1))

        runBlocking { cut.start().join() }

        coVerifyOrder {
            eventHandlerMock1.handleEvent(event1, "someRoomId1")
            eventHandlerMock1.handleEvent(event2, "someRoomId1")
        }
    }

}