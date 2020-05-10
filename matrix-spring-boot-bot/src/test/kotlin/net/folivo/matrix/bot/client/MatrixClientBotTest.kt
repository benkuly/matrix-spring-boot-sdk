package net.folivo.matrix.bot.client

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode.DISABLED
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode.RESTRICTED
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.sync.SyncResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.publisher.TestPublisher

@ExtendWith(MockKExtension::class)
class MatrixClientBotTest {

    @RelaxedMockK
    lateinit var matrixClientMock: MatrixClient

    @RelaxedMockK
    lateinit var eventHandlerMock1: MatrixEventHandler

    @RelaxedMockK
    lateinit var eventHandlerMock2: MatrixEventHandler

    @Test
    fun `should start and call handler`() {
        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(
                        eventHandlerMock1,
                        eventHandlerMock2
                ),
                MatrixBotProperties(serverName = "someServerName")
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

        val publisher = TestPublisher.create<SyncResponse>()
        every { matrixClientMock.syncApi.syncLoop() }.returns(Flux.from(publisher))

        cut.start()
        publisher.next(response1, response2)

        verifyOrder {
            eventHandlerMock1.handleEvent(event1, "someRoomId1")
            eventHandlerMock1.handleEvent(event3, "someRoomId2")
        }
        verifyOrder {
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
                MatrixBotProperties(autoJoin = AutoJoinMode.ENABLED, serverName = "someServerName")
        )

        val response1 = mockk<SyncResponse>(relaxed = true) {
            every { room.invite } returns mapOf(
                    "someRoomId1" to mockk(relaxed = true),
                    "someRoomId2" to mockk(relaxed = true)
            )
        }

        val publisher = TestPublisher.create<SyncResponse>()
        every { matrixClientMock.syncApi.syncLoop() }.returns(Flux.from(publisher))

        cut.start()
        publisher.next(response1)

        val roomsApiMock = matrixClientMock.roomsApi

        verifyOrder {
            roomsApiMock.joinRoom("someRoomId1")
            roomsApiMock.joinRoom("someRoomId2")
        }
        verify(exactly = 0) { roomsApiMock.leaveRoom(any()) }
    }

    @Test
    fun `should not join rooms`() {
        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(),
                MatrixBotProperties(autoJoin = DISABLED, serverName = "someServerName")
        )

        val response1 = mockk<SyncResponse>(relaxed = true) {
            every { room.invite } returns mapOf(
                    "someRoomId1" to mockk(relaxed = true)
            )
        }

        val publisher = TestPublisher.create<SyncResponse>()
        every { matrixClientMock.syncApi.syncLoop() }.returns(Flux.from(publisher))

        cut.start()
        publisher.next(response1)

        val roomsApiMock = matrixClientMock.roomsApi

        verify(exactly = 0) { roomsApiMock.joinRoom(any()) }
        verify { roomsApiMock.leaveRoom("someRoomId1") }
    }

    @Test
    fun `should not join from foreign servers`() {
        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(),
                MatrixBotProperties(autoJoin = RESTRICTED, serverName = "someServerName")
        )

        val response1 = mockk<SyncResponse>(relaxed = true) {
            every { room.invite } returns mapOf(
                    "!someRoomId1:someOtherServer" to mockk(relaxed = true),
                    "!someRoomId2:someServerName" to mockk(relaxed = true)
            )
        }

        val publisher = TestPublisher.create<SyncResponse>()
        every { matrixClientMock.syncApi.syncLoop() }.returns(Flux.from(publisher))

        cut.start()
        publisher.next(response1)

        val roomsApiMock = matrixClientMock.roomsApi

        verify(exactly = 1) {
            roomsApiMock.joinRoom("!someRoomId2:someServerName")
            roomsApiMock.leaveRoom("!someRoomId1:someOtherServer")
        }
    }

    @Test
    fun `should deal with multiple starts`() {
        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(eventHandlerMock1),
                MatrixBotProperties(serverName = "someServerName")
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

        val publisher = TestPublisher.create<SyncResponse>()
        every { matrixClientMock.syncApi.syncLoop() }.returns(Flux.from(publisher))

        cut.start()
        publisher.next(response)
        cut.start()
        publisher.next(response)

        verify(exactly = 2) { eventHandlerMock1.handleEvent(any(), any()) }
    }

    @Test
    fun `should stop`() {
        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(eventHandlerMock1),
                MatrixBotProperties(serverName = "someServerName")
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

        val publisher = TestPublisher.create<SyncResponse>()
        every { matrixClientMock.syncApi.syncLoop() }.returns(Flux.from(publisher))

        cut.start()
        publisher.next(response)
        cut.stop()
        publisher.next(response)

        verify(exactly = 1) { eventHandlerMock1.handleEvent(any(), any()) }
    }

    @Test
    fun `should ignore errors`() {
        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(
                        eventHandlerMock1,
                        eventHandlerMock2
                ),
                MatrixBotProperties(serverName = "someServerName")
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
        every { eventHandlerMock1.handleEvent(any()) } returns Mono.error(RuntimeException())

        val publisher = TestPublisher.create<SyncResponse>()
        every { matrixClientMock.syncApi.syncLoop() }.returns(Flux.from(publisher))

        cut.start()
        publisher.next(response1)

        verifyOrder {
            eventHandlerMock1.handleEvent(event1, "someRoomId1")
            eventHandlerMock1.handleEvent(event2, "someRoomId1")
        }
    }

}