package net.folivo.matrix.bot.appservice

import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verifyAll
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.BAN
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.LEAVE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockKExtension::class)
class LeaveEventHandlerTest {


    @MockK
    lateinit var roomServiceMock: DefaultMatrixAppserviceRoomService

    @BeforeEach
    fun setupMocks() {
        every { roomServiceMock.saveRoomLeave(any(), any()) } returns Mono.empty()
    }

    fun doLeave(userId: String, roomId: String = "@someRoomId:someServerName", membership: Membership = LEAVE) {
        val cut = LeaveEventHandler(
                roomService = roomServiceMock,
                usersRegex = listOf("unicorn_.*"),
                asUsername = "someAsUsername"
        )
        val inviteEvent = mockk<MemberEvent>(relaxed = true) {
            every { content.membership } returns membership
            every { stateKey } returns userId
        }
        StepVerifier
                .create(cut.handleEvent(inviteEvent, roomId))
                .verifyComplete()
    }

    @Test
    fun `should support MemberEvent`() {
        val cut = LeaveEventHandler(
                roomService = roomServiceMock,
                usersRegex = emptyList(),
                asUsername = "someAsUsername"
        )
        assertThat(cut.supports(MemberEvent::class.java)).isTrue()
    }

    @Test
    fun `should save room leave when ban`() {
        doLeave("@someAsUsername:someServerName", membership = BAN)
        verifyAll {
            roomServiceMock.saveRoomLeave("@someRoomId:someServerName", "@someAsUsername:someServerName")
        }
    }

    @Test
    fun `should save room leave when application service user`() {
        doLeave("@someAsUsername:someServerName")
        verifyAll {
            roomServiceMock.saveRoomLeave("@someRoomId:someServerName", "@someAsUsername:someServerName")
        }
    }

    @Test
    fun `should save room leave when managed user`() {
        doLeave("@unicorn_fluffy:someServerName")
        verifyAll {
            roomServiceMock.saveRoomLeave("@someRoomId:someServerName", "@unicorn_fluffy:someServerName")
        }
    }

    @Test
    fun `should not save room leave when no managed user or application service user`() {
        doLeave("@dino_fluffy:someServerName")
        verifyAll {
            roomServiceMock wasNot Called
        }
    }

}