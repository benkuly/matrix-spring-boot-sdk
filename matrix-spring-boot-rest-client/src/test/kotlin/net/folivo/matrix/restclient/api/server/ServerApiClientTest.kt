package net.folivo.matrix.restclient.api.server

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import net.folivo.matrix.restclient.MatrixClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.MediaType

@SpringBootTest(properties = ["matrix.client.homeServer.port=5002"])
class ServerApiClientTest {
    @Autowired
    lateinit var matrixClient: MatrixClient

    @Autowired
    lateinit var objectMapper: ObjectMapper

    val mockWebServer = MockWebServer()

    @BeforeEach
    fun setup() {
        mockWebServer.start(5002)
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `should get versions`() {
        val response = VersionsResponse(
                versions = emptyList(),
                unstable_features = mapOf()
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = runBlocking { matrixClient.serverApi.getVersions() }

        Assertions.assertThat(result).isEqualTo(response)

        val request = mockWebServer.takeRequest()
        Assertions.assertThat(request.path).isEqualTo("/_matrix/client/versions")
        Assertions.assertThat(request.method).isEqualTo(GET.toString())
    }

    @Test
    fun `should get capabilities`() {
        val response = CapabilitiesResponse(
                capabilities = CapabilitiesResponse.Capabilities(
                        CapabilitiesResponse.ChangePasswordCapability(true),
                        CapabilitiesResponse.RoomVersionsCapability(
                                "5",
                                mapOf()
                        )
                )
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = runBlocking { matrixClient.serverApi.getCapabilities() }

        Assertions.assertThat(result).isEqualTo(response)

        val request = mockWebServer.takeRequest()
        Assertions.assertThat(request.path).isEqualTo("/_matrix/client/r0/capabilities")
        Assertions.assertThat(request.method).isEqualTo(GET.toString())
    }
}