package net.folivo.spring.matrix.bot.event

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.folivo.trixnity.core.EventEmitter
import net.folivo.trixnity.core.model.MatrixId.*
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.Event.RoomEvent
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent
import net.folivo.trixnity.core.model.events.m.room.MessageEventContent.TextMessageEventContent

class TestEventEmitter : EventEmitter() {
    suspend fun emitTestEvent(event: Event<*>) {
        emitEvent(event)
    }
}

class EventHandlerRunnerTest : DescribeSpec({
    val eventEmitter: TestEventEmitter = TestEventEmitter()
    val eventHandler1: MatrixEventHandler<MemberEventContent> = mockk(relaxed = true)
    val eventHandler2: MatrixEventHandler<TextMessageEventContent> = mockk(relaxed = true)

    val cut = EventHandlerRunner(eventEmitter, listOf(eventHandler1, eventHandler2))

    describe(EventHandlerRunner::startEventListening.name) {
        beforeTest {
            coEvery { eventHandler1.supports() } returns MemberEventContent::class
            coEvery { eventHandler2.supports() } returns TextMessageEventContent::class
        }

        it("should register event handler and process events") {
            val testEvent = RoomEvent(
                TextMessageEventContent("hoho"),
                EventId("event", "server"),
                UserId("user", "server"),
                RoomId("room", "server"),
                1234L
            )
            val job = GlobalScope.launch {
                cut.startEventListening()
            }
            delay(50)
            eventEmitter.emitTestEvent(testEvent)
            coVerify(exactly = 0) { eventHandler1.handleEvent(any()) }
            coVerify { eventHandler2.handleEvent(testEvent) }
            job.cancel()
        }
    }
})