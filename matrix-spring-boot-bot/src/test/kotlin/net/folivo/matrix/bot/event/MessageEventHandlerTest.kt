package net.folivo.matrix.bot.event

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.coVerifyAll
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import net.folivo.matrix.core.model.MatrixId.*
import net.folivo.matrix.core.model.events.RoomEvent
import net.folivo.matrix.core.model.events.StateEvent
import net.folivo.matrix.core.model.events.m.room.CanonicalAliasEvent
import net.folivo.matrix.core.model.events.m.room.CanonicalAliasEvent.CanonicalAliasEventContent
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import net.folivo.matrix.restclient.MatrixClient

class MessageEventHandlerTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val messageHandlerMock1: MatrixMessageHandler = mockk(relaxed = true)
        val messageHandlerMock2: MatrixMessageHandler = mockk(relaxed = true)
        val matrixClientMock: MatrixClient = mockk(relaxed = true)

        val cut = MessageEventHandler(listOf(messageHandlerMock1, messageHandlerMock2), matrixClientMock)

        describe(MessageEventHandler::supports.name) {
            it("should only support message events") {
                cut.supports(MessageEvent::class.java).shouldBeTrue()
                cut.supports(MemberEvent::class.java).shouldBeFalse()
            }
        }

        describe(MessageEventHandler::handleEvent.name) {
            it("should delegate message events to each handler") {
                val content = TextMessageEventContent("test")
                runBlocking {
                    cut.handleEvent(
                            MessageEvent(
                                    content = content,
                                    roomId = RoomId("room", "server"),
                                    id = EventId("event", "server"),
                                    sender = UserId("sender", "server"),
                                    originTimestamp = 1234,
                                    unsigned = RoomEvent.UnsignedData()
                            ), RoomId("room", "server")
                    )
                }
                coVerifyAll {
                    messageHandlerMock1.handleMessage(content, any())
                    messageHandlerMock2.handleMessage(content, any())
                }
            }
            it("should not delegate non message events") {
                runBlocking {
                    cut.handleEvent(
                            CanonicalAliasEvent(
                                    content = CanonicalAliasEventContent(),
                                    roomId = RoomId("room", "server"),
                                    id = EventId("event", "server"),
                                    sender = UserId("sender", "server"),
                                    originTimestamp = 1234,
                                    unsigned = StateEvent.UnsignedData()
                            ), RoomId("room", "server")
                    )
                }
                coVerifyAll {
                    messageHandlerMock1 wasNot Called
                    messageHandlerMock2 wasNot Called
                }
            }
        }

        afterTest { clearMocks(messageHandlerMock1, messageHandlerMock2, matrixClientMock) }
    }
}