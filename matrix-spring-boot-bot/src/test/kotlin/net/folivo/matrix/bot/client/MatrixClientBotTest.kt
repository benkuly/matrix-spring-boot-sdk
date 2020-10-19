package net.folivo.matrix.bot.client

import io.kotest.assertions.fail
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOf
import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.bot.membership.MembershipChangeHandler
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId
import net.folivo.matrix.core.model.events.m.room.MemberEvent
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
        val eventHandlerMock1: MatrixEventHandler = mockk(relaxed = true, name = "eventHandlerMock1")
        val eventHandlerMock2: MatrixEventHandler = mockk(relaxed = true, name = "eventHandlerMock2")
        val membershipChangeHandlerMock: MembershipChangeHandler = mockk(relaxed = true)
        val helperMock: BotServiceHelper = mockk(relaxed = true)

        val cut = MatrixClientBot(
                matrixClientMock,
                listOf(eventHandlerMock1, eventHandlerMock2),
                membershipChangeHandlerMock,
                helperMock
        )

        val roomId1 = RoomId("room1", "server")
        val roomId2 = RoomId("room2", "server")

        describe(MatrixClientBot::start.name) {
            it("should delegate events to event handlers") {
                val event1 = mockk<MessageEvent<TextMessageEventContent>>()
                val event2 = mockk<MemberEvent>()
                val event3 = mockk<MessageEvent<TextMessageEventContent>>()

                val response1 = mockk<SyncResponse>(relaxed = true) {
                    every { room.join } returns mapOf(
                            roomId1 to mockk(relaxed = true) {
                                every { timeline.events } returns listOf(
                                        event1,
                                        event2
                                )
                            }
                    )
                }
                val response2 = mockk<SyncResponse>(relaxed = true) {
                    every { room.join } returns mapOf(
                            roomId2 to mockk(relaxed = true) {
                                every { timeline.events } returns listOf(
                                        event3
                                )
                            }
                    )
                }

                coEvery { eventHandlerMock1.supports(MessageEvent::class.java) } returns true
                coEvery { eventHandlerMock1.supports(MemberEvent::class.java) } returns false
                coEvery { eventHandlerMock2.supports(MessageEvent::class.java) } returns true
                coEvery { eventHandlerMock2.supports(MemberEvent::class.java) } returns true


                every { matrixClientMock.syncApi.syncLoop() }.returns(flowOf(response1, response2))

                cut.start().join()

                coVerifyOrder {
                    eventHandlerMock1.handleEvent(event1, roomId1)
                    eventHandlerMock2.handleEvent(event1, roomId1)
                    eventHandlerMock2.handleEvent(event2, roomId1)
                    eventHandlerMock1.handleEvent(event3, roomId2)
                    eventHandlerMock2.handleEvent(event3, roomId2)
                }
            }

            it("should delegate invites and leaves to ${MembershipChangeHandler::class.simpleName}") {
                val inviteRoomId1 = RoomId("inviteRoom1", "server")
                val inviteRoomId2 = RoomId("inviteRoom2", "server")
                val leaveRoomId1 = RoomId("leaveRoomId1", "server")
                val leaveRoomId2 = RoomId("leaveRoomId2", "server")
                val botUserId = UserId("bot", "server")

                val response1 = mockk<SyncResponse>(relaxed = true) {
                    every { room.invite } returns mapOf(
                            inviteRoomId1 to mockk(relaxed = true),
                            inviteRoomId2 to mockk(relaxed = true)
                    )
                    every { room.leave } returns mapOf(
                            leaveRoomId1 to mockk(relaxed = true),
                            leaveRoomId2 to mockk(relaxed = true)
                    )
                }

                coEvery { helperMock.getBotUserId() }.returns(botUserId)
                every { matrixClientMock.syncApi.syncLoop() }.returns(flowOf(response1))

                cut.start().join()

                coVerify {
                    membershipChangeHandlerMock.handleMembership(botUserId, inviteRoomId1, INVITE)
                    membershipChangeHandlerMock.handleMembership(botUserId, inviteRoomId2, INVITE)
                    membershipChangeHandlerMock.handleMembership(botUserId, leaveRoomId1, LEAVE)
                    membershipChangeHandlerMock.handleMembership(botUserId, leaveRoomId2, LEAVE)
                }
            }

            it("should deal with multiple starts") {

                val response = mockk<SyncResponse>(relaxed = true) {
                    every { room.join } returns mapOf(
                            roomId1 to mockk(relaxed = true) {
                                every { timeline.events } returns listOf(
                                        mockk<MessageEvent<TextMessageEventContent>>()
                                )
                            }
                    )
                }

                coEvery { eventHandlerMock1.supports(any()) } returns true

                val channel = Channel<SyncResponse>()
                every { matrixClientMock.syncApi.syncLoop() }.returnsMany(
                        channel.consumeAsFlow(),
                        channel.consumeAsFlow()
                )

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
                            roomId1 to mockk(relaxed = true) {
                                every { timeline.events } returns listOf(
                                        event1,
                                        event2
                                )
                            }
                    )
                }

                coEvery { eventHandlerMock1.supports(any()) } returns true
                coEvery { eventHandlerMock2.supports(any()) } returns true
                coEvery { eventHandlerMock1.handleEvent(any()) }.throws(RuntimeException())

                every { matrixClientMock.syncApi.syncLoop() }.returns(flowOf(response1))

                cut.start().join()

                coVerifyOrder {
                    eventHandlerMock1.handleEvent(event1, roomId1)
                    eventHandlerMock1.handleEvent(event2, roomId1)
                }
            }
        }

        describe(MatrixClientBot::stop.name) {
            it("should stop") {
                val response = mockk<SyncResponse>(relaxed = true) {
                    every { room.join } returns mapOf(
                            roomId1 to mockk(relaxed = true) {
                                every { timeline.events } returns listOf(
                                        mockk<MessageEvent<TextMessageEventContent>>()
                                )
                            }
                    )
                }

                coEvery { eventHandlerMock1.supports(any()) } returns true

                val channel = Channel<SyncResponse>()
                every { matrixClientMock.syncApi.syncLoop() }.returns(channel.consumeAsFlow())

                cut.start()
                channel.send(response)
                cut.stop()
                try {
                    channel.send(response)
                    fail("should throw exception")
                } catch (error: CancellationException) {

                }

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