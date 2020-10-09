package net.folivo.matrix.bot.examples.pingappservice

import io.mockk.MockKMatcherScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import net.folivo.matrix.bot.event.MessageContext
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PingHandlerTest {

    @MockK(relaxed = true)
    lateinit var context: MessageContext

    @Test
    fun `should pong after ping message`() {
        val cut = PingHandler()

        coEvery { context.answer(any()) } returns "eventId"

        runBlocking {
            cut.handleMessage(TextMessageEventContent("ping"), context)
            cut.handleMessage(TextMessageEventContent("some ping message"), context)
        }

        coVerify(exactly = 2) { context.answer(messageBody("pong")) }
    }

    private fun MockKMatcherScope.messageBody(expectedBody: String) = match<MessageEvent.MessageEventContent> {
        it.body == expectedBody
    }
}