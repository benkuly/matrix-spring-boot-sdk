package net.folivo.matrix.appservice.config

import net.folivo.matrix.restclient.api.ErrorResponse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody


@SpringBootTest
@AutoConfigureWebTestClient
class MatrixAppserviceWebfluxConfigurationTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @Test
    fun `should deny unauthorized access`() {
        webClient.get().uri("/_matrix/something")
                .exchange()
                .expectStatus().isUnauthorized
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody<ErrorResponse>().isEqualTo(ErrorResponse("401", "M_FORBIDDEN"))
    }
}