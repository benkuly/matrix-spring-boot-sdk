package net.folivo.matrix.appservice.config

import net.folivo.matrix.common.api.ErrorResponse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody


@SpringBootTest
@AutoConfigureWebTestClient
class MatrixAppserviceWebfluxConfigurationTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @Test
    fun `should forbid missing token`() {
        webClient.get().uri("/_matrix/something").accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody<ErrorResponse>().isEqualTo(ErrorResponse("403", "M_FORBIDDEN"))
    }

    @Test
    fun `should forbid wrong token`() {
        webClient.get().uri("/_matrix/something?access_token=invalidToken").accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody<ErrorResponse>().isEqualTo(ErrorResponse("403", "M_FORBIDDEN"))
    }

    @Test
    fun `should permit right token`() {
        webClient.get().uri("/_matrix/something?access_token=validToken").accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
    }
}