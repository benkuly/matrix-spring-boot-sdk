package net.folivo.matrix.bot.client

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOf
import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.bot.membership.MembershipChangeHandler
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.INVITE
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.LEAVE
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.sync.SyncResponse

class MatrixClientBotTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val matrixClientMock: MatrixClient = mockk(relaxed = true)
        val eventHandlerMock1: MatrixEventHandler = mockk(relaxed = true)
        val eventHandlerMock2: MatrixEventHandler = mockk(relaxed = true)
        val membershipChangeHandlerMock: MembershipChangeHandler = mockk(relaxed = true)
        val helperMock: BotServiceHelper = mockk(relaxed = true)

        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(eventHandlerMock1, eventHandlerMock1),
                membershipChangeHandlerMock,
                helperMock
        )

        describe(MatrixClientBot::start.name) {
            it("should delegate events to event handlers") {
                val event1 = mockk<MessageEvent<TextMessageEventContent>>()
                val event2 = mockk<MessageEvent<TextMessageEventContent>>()
                val event3 = mockk<MessageEvent<TextMessageEventContent>>()

                val response1 = mockk<SyncResponse>(relaxed = true) {
                    every { room.join } returns mapOf(
                            "someRoomId1" to mockk(relaxed = true) {
                                every { timeline.events } returns listOf(
                                        event1,
                                        event2
                                )
                            }
                    )
                }
                val response2 = mockk<SyncResponse>(relaxed = true) {
                    every { room.join } returns mapOf(
                            "someRoomId2" to mockk(relaxed = true) {
                                every { timeline.events } returns listOf(
                                        event3
                                )
                            }
                    )
                }

                every { eventHandlerMock1.supports(any()) } returns true
                every { eventHandlerMock1.supports(any()) } returns false
                every { eventHandlerMock1.supports(any()) } returns true
                every { eventHandlerMock2.supports(any()) } returns true

                every { matrixClientMock.syncApi.syncLoop() }.returns(flowOf(response1, response2))

                cut.start().join()

                coVerifyOrder {
                    eventHandlerMock1.handleEvent(event1, "someRoomId1")
                    eventHandlerMock1.handleEvent(event3, "someRoomId2")
                }
                coVerifyOrder {
                    eventHandlerMock2.handleEvent(event1, "someRoomId1")
                    eventHandlerMock2.handleEvent(event2, "someRoomId1")
                    eventHandlerMock2.handleEvent(event3, "someRoomId2")
                }
            }

            it("should delegate invites and leaves to ${MembershipChangeHandler::class.simpleName}") {
                val response1 = mockk<SyncResponse>(relaxed = true) {
                    every { room.invite } returns mapOf(
                            "inviteRoom1" to mockk(relaxed = true),
                            "inviteRoom2" to mockk(relaxed = true)
                    )
                    every { room.leave } returns mapOf(
                            "leaveRoom1" to mockk(relaxed = true),
                            "leaveRoom2" to mockk(relaxed = true)
                    )
                }

                coEvery { helperMock.getBotUserId() }.returns("@bot:someServer")
                every { matrixClientMock.syncApi.syncLoop() }.returns(flowOf(response1))

                cut.start().join()

                coVerify {
                    membershipChangeHandlerMock.handleMembership("inviteRoom1", "@bot:someServer", INVITE)
                    membershipChangeHandlerMock.handleMembership("inviteRoom2", "@bot:someServer", INVITE)
                    membershipChangeHandlerMock.handleMembership("leaveRoom1", "@bot:someServer", LEAVE)
                    membershipChangeHandlerMock.handleMembership("leaveRoom2", "@bot:someServer", LEAVE)
                }
            }

            it("should deal with multiple starts") {

                val response = mockk<SyncResponse>(relaxed = true) {
                    every { room.join } returns mapOf(
                            "roomId" to mockk(relaxed = true) {
                                every { timeline.events } returns listOf(
                                        mockk<MessageEvent<TextMessageEventContent>>()
                                )
                            }
                    )
                }

                every { eventHandlerMock1.supports(any()) } returns true

                val channel = Channel<SyncResponse>()
                every { matrixClientMock.syncApi.syncLoop() }.returns(channel.consumeAsFlow())

                cut.start()
                cut.start()
                channel.send(response)
                channel.send(response)
                cut.stop()

                coVerify(exactly = 2) { eventHandlerMock1.handleEvent(any(), any()) }
            }

            it("should ignore errors") {
                val event1 = mockk<MessageEvent<TextMessageEventContent>>()
                val event2 = mockk<MessageEvent<TextMessageEventContent>>()

                val response1 = mockk<SyncResponse>(relaxed = true) {
                    every { room.join } returns mapOf(
                            "someRoomId1" to mockk(relaxed = true) {
                                every { timeline.events } returns listOf(
                                        event1,
                                        event2
                                )
                            }
                    )
                }

                every { eventHandlerMock1.supports(any()) } returns true
                every { eventHandlerMock2.supports(any()) } returns true
                coEvery { eventHandlerMock1.handleEvent(any()) }.throws(RuntimeException())

                every { matrixClientMock.syncApi.syncLoop() }.returns(flowOf(response1))

                cut.start().join()

                coVerifyOrder {
                    eventHandlerMock1.handleEvent(event1, "someRoomId1")
                    eventHandlerMock1.handleEvent(event2, "someRoomId1")
                }
            }
        }

        describe(MatrixClientBot::stop.name) {
            it("should stop") {
                val response = mockk<SyncResponse>(relaxed = true) {
                    every { room.join } returns mapOf(
                            "roomId" to mockk(relaxed = true) {
                                every { timeline.events } returns listOf(
                                        mockk<MessageEvent<TextMessageEventContent>>()
                                )
                            }
                    )
                }

                every { eventHandlerMock1.supports(any()) } returns true

                val channel = Channel<SyncResponse>()
                every { matrixClientMock.syncApi.syncLoop() }.returns(channel.consumeAsFlow())

                cut.start()
                channel.send(response)
                cut.stop()
                channel.send(response)

                coVerify(exactly = 1) { eventHandlerMock1.handleEvent(any(), any()) }
            }
        }

        afterTest {
            clearMocks(
                    matrixClientMock,
                    eventHandlerMock1,
                    eventHandlerMock1,
                    membershipChangeHandlerMock,
                    helperMock
            )
        }
    }
}