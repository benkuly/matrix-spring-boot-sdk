package net.folivo.matrix.appservice

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.just
import net.folivo.matrix.appservice.api.AppserviceHandler
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
        coEvery { appserviceHandlerMock.addTransactions("1", any()) } just Runs

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
    fun `addTransactions should handle null events`() {
        coEvery { appserviceHandlerMock.addTransactions("1", any()) } just Runs

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
        coEvery { appserviceHandlerMock.hasRoomAlias("someRoomAlias") } returns false

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
        coEvery { appserviceHandlerMock.hasUser("someUserId") } returns true

        webTestClient.get()
                .uri("/_matrix/app/v1/users/someUserId?access_token=validToken")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().json("{}")
    }

    @Test
    fun `hasUser should return 404 when handler is false`() {
        coEvery { appserviceHandlerMock.hasUser("someUserId") } returns false

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
        coEvery { appserviceHandlerMock.hasRoomAlias("someRoomAlias") } returns true

        webTestClient.get()
                .uri("/_matrix/app/v1/rooms/someRoomAlias?access_token=validToken")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody().json("{}")
    }

    @Test
    fun `hasRoomAlias should return 404 when handler is false`() {
        coEvery { appserviceHandlerMock.hasRoomAlias("someRoomAlias") } returns false

        webTestClient.get()
                .uri("/_matrix/app/v1/rooms/someRoomAlias?access_token=validToken")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound
                .expectBody()
                .jsonPath("$.errcode").isEqualTo("NET.FOLIVO.MATRIX_NOT_FOUND")
    }
}
