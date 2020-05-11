package net.folivo.matrix.bot.examples.ping

import io.mockk.MockKMatcherScope
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import net.folivo.matrix.bot.handler.MessageContext
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Mono

@ExtendWith(MockKExtension::class)
class PingHandlerText {

    @MockK(relaxed = true)
    lateinit var context: MessageContext

    @Test
    fun `should pong after ping message`() {
        val cut = PingHandler()

        every { context.answer(any()) } returns Mono.just("eventId")

        cut.handleMessage(TextMessageEventContent("ping"), context).subscribe()
        cut.handleMessage(TextMessageEventContent("some ping message"), context).subscribe()

        verify(exactly = 2) { context.answer(messageBody("pong")) }
    }

    private fun MockKMatcherScope.messageBody(expectedBody: String) = match<MessageEvent.MessageEventContent> {
        it.body == expectedBody
    }
}