package net.folivo.matrix.bot.appservice

import io.mockk.*
import kotlinx.coroutines.runBlocking
import net.folivo.matrix.bot.appservice.event.DefaultAppserviceEventService
import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import org.junit.jupiter.api.Test

class DefaultAppserviceEventServiceTest {

    @Test
    fun `should process event and delegate to event handler`() {
        val eventHandler1 = mockk<MatrixEventHandler> {
            every { supports(any()) } returns true
            coEvery { handleEvent(any(), any()) } just Runs
        }
        val event = mockk<MessageEvent<TextMessageEventContent>>()
        every { event.roomId } returns "someRoomId"

        val cut = DefaultAppserviceEventService(listOf(eventHandler1))

        runBlocking { cut.processEvent(event) }

        verify {
            eventHandler1.supports(MessageEvent::class.java)
        }
        coVerify {
            eventHandler1.handleEvent(event, "someRoomId")
        }
    }
}