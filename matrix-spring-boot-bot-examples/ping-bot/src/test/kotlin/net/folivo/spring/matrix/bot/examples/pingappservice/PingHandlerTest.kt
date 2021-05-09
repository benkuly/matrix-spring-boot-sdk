package net.folivo.spring.matrix.bot.examples.pingappservice

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import net.folivo.spring.matrix.bot.event.MessageContext
import net.folivo.trixnity.core.model.MatrixId
import net.folivo.trixnity.core.model.events.m.room.MessageEventContent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PingHandlerTest {

    @MockK(relaxed = true)
    lateinit var context: MessageContext

    @Test
    fun `should pong after ping message`() {
        val cut = PingHandler()

        coEvery { context.answer(any<String>()) } returns MatrixId.EventId("event", "server")

        runBlocking {
            cut.handleMessage(MessageEventContent.TextMessageEventContent("ping"), context)
            cut.handleMessage(MessageEventContent.TextMessageEventContent("some ping message"), context)
        }

        coVerify(exactly = 2) { context.answer("pong") }
    }
}