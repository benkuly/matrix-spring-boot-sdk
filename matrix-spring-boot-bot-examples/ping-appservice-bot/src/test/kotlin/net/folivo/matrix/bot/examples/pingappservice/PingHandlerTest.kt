package net.folivo.matrix.bot.examples.pingappservice

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import net.folivo.matrix.bot.appservice.room.AppserviceRoom
import net.folivo.matrix.bot.appservice.room.AppserviceRoomRepository
import net.folivo.matrix.bot.appservice.user.AppserviceUser
import net.folivo.matrix.bot.handler.MessageContext
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Mono

@ExtendWith(MockKExtension::class)
class PingHandlerTest {

    @MockK
    lateinit var roomRepositoryMock: AppserviceRoomRepository

    @MockK
    lateinit var context: MessageContext

    @Test
    fun `should pong after ping message`() {
        every { roomRepositoryMock.findById("someRoomId") }.returns(
                Mono.just(
                        AppserviceRoom(
                                "someRoomId",
                                members = mutableSetOf(AppserviceUser("someUserId1"), AppserviceUser("someUserId2"))
                        )
                )
        )

        val cut = PingHandler(roomRepositoryMock)

        every { context.answer(any(), any()) } returns Mono.just("eventId")
        every { context.roomId } returns "someRoomId"

        cut.handleMessage(TextMessageEventContent("ping"), context).subscribe()
        cut.handleMessage(TextMessageEventContent("some ping message"), context).subscribe()

        verify(exactly = 2) {
            context.answer(match { it.body == "pong" }, asUserId = "someUserId1")
        }
        verify(exactly = 2) {
            context.answer(match { it.body == "pong" }, asUserId = "someUserId2")
        }
    }
}