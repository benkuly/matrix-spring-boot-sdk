package net.folivo.matrix.bot.appservice.event

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import net.folivo.matrix.appservice.api.event.AppserviceEventService.EventProcessingState.NOT_PROCESSED
import net.folivo.matrix.appservice.api.event.AppserviceEventService.EventProcessingState.PROCESSED
import net.folivo.matrix.bot.appservice.sync.MatrixSyncService
import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.core.model.MatrixId.EventId
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.INVITE
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent

class DefaultAppserviceEventServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {

        val eventTransactionServiceMock = mockk<MatrixEventTransactionService>(relaxed = true)
        val syncServiceMock = mockk<MatrixSyncService>(relaxed = true)
        val eventHandlerMock1 = mockk<MatrixEventHandler>(relaxed = true)
        val eventHandlerMock2 = mockk<MatrixEventHandler>(relaxed = true)

        val cut = DefaultAppserviceEventService(
                eventTransactionServiceMock,
                syncServiceMock,
                listOf(eventHandlerMock1, eventHandlerMock2)
        )

        describe(DefaultAppserviceEventService::eventProcessingState.name) {
            describe("event already processed") {
                it("should return $PROCESSED") {
                    coEvery { eventTransactionServiceMock.hasEvent("someTnxId", EventId("event", "server")) }
                            .returns(true)
                    cut.eventProcessingState("someTnxId", EventId("event", "server"))
                            .shouldBe(PROCESSED)
                }
            }
            describe("event not processed") {
                it("should return $NOT_PROCESSED") {
                    coEvery { eventTransactionServiceMock.hasEvent("someTnxId", EventId("event", "server")) }
                            .returns(false)
                    cut.eventProcessingState("someTnxId", EventId("event", "server"))
                            .shouldBe(NOT_PROCESSED)
                }
            }
        }
        describe(DefaultAppserviceEventService::onEventProcessed.name) {
            it("should save event as processed") {
                cut.onEventProcessed("someTnxId", EventId("event", "server"))

                coVerify {
                    eventTransactionServiceMock.saveEvent(
                            MatrixEventTransaction(
                                    "someTnxId",
                                    EventId("event", "server")
                            )
                    )
                }
            }
        }
        describe(DefaultAppserviceEventService::processEvent.name) {
            beforeTest {
                coEvery { eventHandlerMock1.supports(any()) }.returns(true)
                coEvery { eventHandlerMock2.supports(any()) }.returns(false)
            }

            val event = mockk<MessageEvent<*>> {
                every { roomId } returns RoomId("room", "server")
            }
            it("should sync room when event is not invite member event") {
                cut.processEvent(event)
                coVerify { syncServiceMock.syncRoomMemberships(RoomId("room", "server")) }
            }
            it("should not sync room when event is invite member event") {
                val memberEvent = mockk<MemberEvent> {
                    every { roomId } returns RoomId("room", "server")
                    every { content.membership } returns INVITE
                }
                cut.processEvent(memberEvent)
                coVerify(exactly = 0) { syncServiceMock.syncRoomMemberships(any()) }
            }
            it("should delegate to matching event handler") {
                cut.processEvent(event)
                coVerify { eventHandlerMock1.handleEvent(event, RoomId("room", "server")) }
            }
            it("should not delegate to not matching event handler") {
                cut.processEvent(event)
                coVerify(exactly = 0) { eventHandlerMock2.handleEvent(any(), any()) }
            }
        }

        afterTest {
            clearMocks(
                    eventTransactionServiceMock,
                    syncServiceMock,
                    eventHandlerMock1, eventHandlerMock2
            )
        }
    }
}