package net.folivo.matrix.restclient.config

import com.fasterxml.jackson.databind.ObjectMapper
import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.core.api.MatrixServerException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.test.StepVerifier


@SpringBootTest(properties = ["matrix.client.homeServer.port=5000"])
class MatrixClientAutoconfigurationTest {

    @Autowired
    lateinit var matrixWebClient: WebClient

    @Autowired
    lateinit var objectMapper: ObjectMapper

    val mockWebServer = MockWebServer()

    @BeforeEach
    fun setup() {
        mockWebServer.start(5000)
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `matrixWebClient should resolve to base url with json header and authentication bearer`() {
        mockWebServer.enqueue(MockResponse())

        matrixWebClient.get().uri("/somePath").retrieve().toBodilessEntity().block()

        val request = mockWebServer.takeRequest()
        assertThat(request.headers.get(CONTENT_TYPE)).isEqualTo(APPLICATION_JSON_VALUE)
        assertThat(request.headers.get(AUTHORIZATION)).isEqualTo("Bearer superSecretToken")
        assertThat(request.path).isEqualTo("/_matrix/client/somePath")
    }

    @Test
    fun `should filter error status codes and create custom exception`() {
        val errorResponse = ErrorResponse("500", "we have no bananas")
        mockWebServer.enqueue(
                MockResponse()
                        .setResponseCode(500)
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(errorResponse))
        )

        val response = matrixWebClient.get().uri("/somePath").exchangeToMono { _ -> Mono.empty<Unit>() }

        StepVerifier.create(response).consumeErrorWith {
            if (it is MatrixServerException) {
                assertThat(it.errorResponse).isEqualTo(errorResponse)
            } else {
                fail("error should be of type ${MatrixServerException::class} but was ${it::class.java}")
            }
        }.verify()

    }
}