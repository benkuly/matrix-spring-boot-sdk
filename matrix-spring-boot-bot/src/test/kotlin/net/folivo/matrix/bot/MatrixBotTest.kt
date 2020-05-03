package net.folivo.matrix.bot

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.core.handler.MatrixEventHandler
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.sync.SyncResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Flux
import reactor.test.publisher.TestPublisher

@ExtendWith(MockKExtension::class)
class MatrixBotTest {

    @RelaxedMockK
    lateinit var matrixClientMock: MatrixClient

    @RelaxedMockK
    lateinit var eventHandlerMock1: MatrixEventHandler

    @RelaxedMockK
    lateinit var eventHandlerMock2: MatrixEventHandler

    @Test
    fun `should start and call handler`() {
        val cut = MatrixBot(matrixClientMock, listOf(eventHandlerMock1, eventHandlerMock2), MatrixBotProperties())

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
        val cut = MatrixBot(matrixClientMock, listOf(), MatrixBotProperties(autojoin = true))

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

        verifyOrder {
            matrixClientMock.roomsApi.joinRoom("someRoomId1")
            matrixClientMock.roomsApi.joinRoom("someRoomId2")
        }
    }

    @Test
    fun `should not join rooms`() {
        val cut = MatrixBot(matrixClientMock, listOf(), MatrixBotProperties(autojoin = false))

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

        verify(exactly = 0) {
            matrixClientMock.roomsApi.joinRoom(any())
        }
    }

    @Test
    fun `should deal with multiple starts`() {
        val cut = MatrixBot(matrixClientMock, listOf(eventHandlerMock1), MatrixBotProperties())

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
        val cut = MatrixBot(matrixClientMock, listOf(eventHandlerMock1), MatrixBotProperties())

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

}