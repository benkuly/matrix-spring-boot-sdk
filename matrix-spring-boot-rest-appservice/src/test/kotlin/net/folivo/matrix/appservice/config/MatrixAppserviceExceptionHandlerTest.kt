package net.folivo.matrix.appservice.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus.I_AM_A_TEAPOT
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
class MatrixAppserviceExceptionHandlerTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @Test
    fun `should permit right token`() {
        webClient.get().uri("/someError?access_token=validToken").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(I_AM_A_TEAPOT)
                .expectBody().json("""{"errcode":"TEA_PLEASE"}""")
    }
}