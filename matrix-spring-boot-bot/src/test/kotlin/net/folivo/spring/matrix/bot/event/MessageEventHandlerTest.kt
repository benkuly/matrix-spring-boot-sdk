package net.folivo.spring.matrix.bot.event

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coVerifyAll
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.core.model.MatrixId
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.UnsignedData
import net.folivo.trixnity.core.model.events.m.room.MessageEventContent

class MessageEventHandlerTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val messageHandlerMock1: MatrixMessageHandler = mockk(relaxed = true)
        val messageHandlerMock2: MatrixMessageHandler = mockk(relaxed = true)
        val matrixClientMock: MatrixClient = mockk(relaxed = true)

        val cut = MessageEventHandler(listOf(messageHandlerMock1, messageHandlerMock2), matrixClientMock)

        describe(MessageEventHandler::supports.name) {
            it("should only support message events") {
                cut.supports().shouldBe(MessageEventContent::class)
            }
        }

        describe(MessageEventHandler::handleEvent.name) {
            it("should delegate message events to each handler") {
                val content = MessageEventContent.TextMessageEventContent("test")
                runBlocking {
                    cut.handleEvent(
                        Event.RoomEvent(
                            content = content,
                            roomId = MatrixId.RoomId("room", "server"),
                            id = MatrixId.EventId("event", "server"),
                            sender = MatrixId.UserId("sender", "server"),
                            originTimestamp = 1234,
                            unsigned = UnsignedData()
                        )
                    )
                }
                coVerifyAll {
                    messageHandlerMock1.handleMessage(content, any())
                    messageHandlerMock2.handleMessage(content, any())
                }
            }
        }

        afterTest { clearMocks(messageHandlerMock1, messageHandlerMock2, matrixClientMock) }
    }
}