package net.folivo.matrix.bot.appservice.event

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import net.folivo.matrix.appservice.api.event.AppserviceEventService.EventProcessingState.NOT_PROCESSED
import net.folivo.matrix.appservice.api.event.AppserviceEventService.EventProcessingState.PROCESSED
import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent

class DefaultAppserviceEventServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {

        val eventTransactionServiceMock = mockk<MatrixEventTransactionService>(relaxed = true)
        val eventHandlerMock1 = mockk<MatrixEventHandler>(relaxed = true)
        val eventHandlerMock2 = mockk<MatrixEventHandler>(relaxed = true)

        val cut = DefaultAppserviceEventService(
                eventTransactionServiceMock,
                listOf(eventHandlerMock1, eventHandlerMock2)
        )

        describe(DefaultAppserviceEventService::eventProcessingState.name) {
            describe("event already processed") {
                coEvery { eventTransactionServiceMock.hasEvent("someTnxId", "someEventId") }
                        .returns(true)

                it("should return $PROCESSED") {
                    cut.eventProcessingState("someTnxId", "someEventId")
                            .shouldBe(PROCESSED)
                }
            }
            describe("event not processed") {
                coEvery { eventTransactionServiceMock.hasEvent("someTnxId", "someEventId") }
                        .returns(false)

                it("should return $NOT_PROCESSED") {
                    cut.eventProcessingState("someTnxId", "someEventId")
                            .shouldBe(NOT_PROCESSED)
                }
            }
        }
        describe(DefaultAppserviceEventService::onEventProcessed.name) {
            it("should save event as processed") {
                cut.onEventProcessed("someTnxId", "someEventId")

                coVerify { eventTransactionServiceMock.saveEvent(MatrixEventTransaction("someTnxId", "someEventId")) }
            }
        }
        describe(DefaultAppserviceEventService::processEvent.name) {
            coEvery { eventHandlerMock1.supports(any()) }.returns(true)
            coEvery { eventHandlerMock2.supports(any()) }.returns(false)

            val event = mockk<MessageEvent<*>> {
                every { roomId } returns "someRoomId"
            }
            cut.processEvent(event)
            it("should delegate to matching event handler") {
                coVerify { eventHandlerMock1.handleEvent(event, "someRoomId") }
            }
            it("should not delegate to not matching event handler") {
                coVerify(exactly = 0) { eventHandlerMock2.handleEvent(any(), any()) }
            }
        }
    }
}