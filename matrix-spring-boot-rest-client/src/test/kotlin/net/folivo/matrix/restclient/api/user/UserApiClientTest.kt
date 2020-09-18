package net.folivo.matrix.restclient.api.user

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.netty.handler.codec.http.HttpMethod.PUT
import kotlinx.coroutines.runBlocking
import net.folivo.matrix.restclient.MatrixClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.GET
import org.springframework.http.MediaType

@SpringBootTest(properties = ["matrix.client.homeServer.port=5004"])
class UserApiClientTest {
    @Autowired
    lateinit var matrixClient: MatrixClient

    @Autowired
    lateinit var objectMapper: ObjectMapper

    val mockWebServer = MockWebServer()

    @BeforeEach
    fun setup() {
        mockWebServer.start(5004)
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `should register`() {
        val response = RegisterResponse("someUserId")
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = runBlocking {
            matrixClient.userApi.register(
                    authenticationType = "someAuthenticationType",
                    authenticationSession = "someAuthenticationSession",
                    username = "someUsername",
                    password = "somePassword",
                    accountType = AccountType.USER,
                    deviceId = "someDeviceId",
                    initialDeviceDisplayName = "someInitialDeviceDisplayName",
                    inhibitLogin = true
            )
        }

        assertThat(result).isEqualTo(response)

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/register?kind=user")

        val expectedRequest = """
            {
              "auth" : {
                "type" : "someAuthenticationType",
                "session" : "someAuthenticationSession"
              },
              "username" : "someUsername",
              "password" : "somePassword",
              "device_id" : "someDeviceId",
              "initial_device_display_name" : "someInitialDeviceDisplayName",
              "inhibit_login" : true
            }
        """.trimIndent()
        assertThat(request.body.readUtf8()).isEqualTo(
                objectMapper.readValue<JsonNode>(expectedRequest).toString()
        )
        assertThat(request.method).isEqualTo(HttpMethod.POST.toString())
    }

    @Test
    fun `should set display name`() {
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("{}")
        )

        runBlocking { matrixClient.userApi.setDisplayName("someUserId", "someDisplayName") }

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/profile/someUserId/displayname")
        assertThat(request.body.readUtf8()).isEqualTo("""{"displayname":"someDisplayName"}""")
        assertThat(request.method).isEqualTo(PUT.toString())
    }

    @Test
    fun `should get whoami`() {
        val response = WhoAmIResponse("someUserId")
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = runBlocking { matrixClient.userApi.whoAmI() }

        assertThat(result).isEqualTo("someUserId")

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/account/whoami")
        assertThat(request.method).isEqualTo(GET.toString())
    }
}