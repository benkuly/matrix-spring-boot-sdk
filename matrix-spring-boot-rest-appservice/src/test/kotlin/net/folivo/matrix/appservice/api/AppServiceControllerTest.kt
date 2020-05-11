package net.folivo.matrix.appservice.api

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.junit5.MockKExtension
import net.folivo.matrix.core.model.events.RoomEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@SpringBootTest
@AutoConfigureWebTestClient
@ExtendWith(MockKExtension::class)
class AppServiceControllerTest {

    @MockkBean
    lateinit var appserviceHandlerMock: AppserviceHandler

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `addTransactions should return 200 when handler is okay`() {
        every { appserviceHandlerMock.addTransactions("1", any()) } returns Mono.empty()

        webTestClient.put()
                .uri("/_matrix/app/v1/transactions/1?access_token=validToken")
                .bodyValue(
                        mapOf(
                                "events" to listOf(
                                        MessageEvent<TextMessageEventContent>(
                                                TextMessageEventContent("hello"),
                                                "someId",
                                                "someSender",
                                                123,
                                                "someRoomId",
                                                RoomEvent.UnsignedData()
                                        )
                                )
                        )
                )
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().json("{}")
    }

    @Test
    fun `addTransactions should return 404 when handler is false`() {
        every { appserviceHandlerMock.hasRoomAlias("someRoomAlias") } returns Mono.just(false)

        webTestClient.get()
                .uri("/_matrix/app/v1/rooms/someRoomAlias?access_token=validToken")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound
                .expectBody()
                .jsonPath("$.errcode").isEqualTo("NET.FOLIVO.MATRIX_NOT_FOUND")
    }

    @Test
    fun `hasUser should return 200 when handler is true`() {
        every { appserviceHandlerMock.hasUser("someUserId") } returns Mono.just(true)

        webTestClient.get()
                .uri("/_matrix/app/v1/users/someUserId?access_token=validToken")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().json("{}")
    }

    @Test
    fun `hasUser should return 404 when handler is false`() {
        every { appserviceHandlerMock.hasUser("someUserId") } returns Mono.just(false)

        webTestClient.get()
                .uri("/_matrix/app/v1/users/someUserId?access_token=validToken")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound
                .expectBody()
                .jsonPath("$.errcode").isEqualTo("NET.FOLIVO.MATRIX_NOT_FOUND")
    }

    @Test
    fun `hasRoomAlias should return 200 when handler is true`() {
        every { appserviceHandlerMock.hasRoomAlias("someRoomAlias") } returns Mono.just(true)

        webTestClient.get()
                .uri("/_matrix/app/v1/rooms/someRoomAlias?access_token=validToken")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().json("{}")
    }

    @Test
    fun `hasRoomAlias should return 404 when handler is false`() {
        every { appserviceHandlerMock.hasRoomAlias("someRoomAlias") } returns Mono.just(false)

        webTestClient.get()
                .uri("/_matrix/app/v1/rooms/someRoomAlias?access_token=validToken")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound
                .expectBody()
                .jsonPath("$.errcode").isEqualTo("NET.FOLIVO.MATRIX_NOT_FOUND")
    }
}
