package net.folivo.matrix.bot.handler

import io.mockk.Called
import io.mockk.coVerifyAll
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import net.folivo.matrix.core.model.events.RoomEvent
import net.folivo.matrix.core.model.events.StateEvent
import net.folivo.matrix.core.model.events.m.room.AliasesEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import net.folivo.matrix.restclient.MatrixClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MatrixMessageEventHandlerTest {

    @MockK
    lateinit var matrixClientMock: MatrixClient

    @RelaxedMockK
    lateinit var messageContentHandler1: MatrixMessageContentHandler

    @RelaxedMockK
    lateinit var messageContentHandler2: MatrixMessageContentHandler

    @Test
    fun `should support message events`() {
        val cut = MatrixMessageEventHandler(listOf(), matrixClientMock)
        assertThat(cut.supports(MessageEvent::class.java)).isTrue()
    }

    @Test
    fun `should delegate message events to each handler`() {
        val cut = MatrixMessageEventHandler(
                listOf(
                        messageContentHandler1,
                        messageContentHandler2
                ), matrixClientMock
        )
        val content = TextMessageEventContent("test")
        runBlocking {
            cut.handleEvent(
                    MessageEvent(
                            content = content,
                            roomId = "someRoomId",
                            id = "someMessageId",
                            sender = "someSender",
                            originTimestamp = 1234,
                            unsigned = RoomEvent.UnsignedData()
                    ), "roomId"
            )
        }
        coVerifyAll {
            messageContentHandler1.handleMessage(content, any())
            messageContentHandler2.handleMessage(content, any())
        }
    }

    @Test
    fun `should not delegate non message events`() {
        val cut = MatrixMessageEventHandler(
                listOf(
                        messageContentHandler1,
                        messageContentHandler2
                ), matrixClientMock
        )
        runBlocking {
            cut.handleEvent(
                    AliasesEvent(
                            content = AliasesEvent.AliasesEventContent(),
                            roomId = "someRoomId",
                            id = "someMessageId",
                            sender = "someSender",
                            originTimestamp = 1234,
                            stateKey = "",
                            unsigned = StateEvent.UnsignedData()
                    ), "roomId"
            )
        }
        coVerifyAll {
            messageContentHandler1 wasNot Called
            messageContentHandler2 wasNot Called
        }
    }
}