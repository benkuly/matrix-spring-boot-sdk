package net.folivo.matrix.bot.appservice

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class DefaultMatrixAppserviceEventServiceTest {

    @Test
    fun `should process event and delegate to event handler`() {
        val eventHandler1 = mockk<MatrixEventHandler> {
            every { supports(any()) } returns true
            every { handleEvent(any(), any()) } returns Mono.empty()
        }
        val event = mockk<MessageEvent<TextMessageEventContent>>()
        every { event.roomId } returns "someRoomId"

        val cut = DefaultMatrixAppserviceEventService(listOf(eventHandler1))

        StepVerifier
                .create(cut.processEvent(event))
                .verifyComplete()

        verifyAll {
            eventHandler1.supports(MessageEvent::class.java)
            eventHandler1.handleEvent(event, "someRoomId")
        }

    }
}