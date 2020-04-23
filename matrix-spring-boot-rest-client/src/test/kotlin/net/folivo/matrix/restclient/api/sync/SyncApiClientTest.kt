package net.folivo.matrix.restclient.api.sync

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.verifyOrder
import net.folivo.matrix.restclient.MatrixClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import reactor.test.StepVerifier

@SpringBootTest(properties = ["matrix.homeServer.port=5003"])
@ExtendWith(MockKExtension::class)
class SyncApiClientTest {
    @Autowired
    lateinit var matrixClient: MatrixClient

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean(relaxed = true)
    lateinit var syncBatchTokenService: SyncBatchTokenService

    val mockWebServer = MockWebServer()

    @BeforeEach
    fun setup() {
        mockWebServer.start(5003)
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `should syncOnce`() {
        val response = SyncResponse(
                nextBatch = "nextBatch",
                accountData = SyncResponse.AccountData(emptyList()),
                deviceLists = SyncResponse.DeviceLists(emptyList(), emptyList()),
                deviceOneTimeKeysCount = emptyMap(),
                presence = SyncResponse.Presence(emptyList()),
                room = SyncResponse.Rooms(emptyMap(), emptyMap(), emptyMap()),
                toDevice = SyncResponse.ToDevice(emptyList())
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = matrixClient.syncApi.syncOnce(
                filter = "someFilter",
                fullState = true,
                setPresence = Presence.ONLINE,
                since = "someSince",
                timeout = 1234
        ).block()

        assertThat(result).isEqualTo(response)

        val request = mockWebServer.takeRequest()
        assertThat(request.path)
                .isEqualTo("/_matrix/client/r0/sync?filter=someFilter&full_state=true&set_presence=online&since=someSince&timeout=1234")
        assertThat(request.method).isEqualTo(HttpMethod.GET.toString())
    }

    @Test
    fun `should syncLoop`() {
        val response1 = SyncResponse(
                nextBatch = "nextBatch1",
                accountData = SyncResponse.AccountData(emptyList()),
                deviceLists = SyncResponse.DeviceLists(emptyList(), emptyList()),
                deviceOneTimeKeysCount = emptyMap(),
                presence = SyncResponse.Presence(emptyList()),
                room = SyncResponse.Rooms(emptyMap(), emptyMap(), emptyMap()),
                toDevice = SyncResponse.ToDevice(emptyList())
        )
        val response2 = SyncResponse(
                nextBatch = "nextBatch2",
                accountData = SyncResponse.AccountData(emptyList()),
                deviceLists = SyncResponse.DeviceLists(emptyList(), emptyList()),
                deviceOneTimeKeysCount = emptyMap(),
                presence = SyncResponse.Presence(emptyList()),
                room = SyncResponse.Rooms(emptyMap(), emptyMap(), emptyMap()),
                toDevice = SyncResponse.ToDevice(emptyList())
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response1))
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response2))
        )

        every { syncBatchTokenService.batchToken }.returns("nextBatch0")

        val result = matrixClient.syncApi.syncLoop(
                filter = "someFilter",
                setPresence = Presence.ONLINE
        )

        StepVerifier.create(result, 2)
                .expectNext(response1)
                .expectNext(response2)
                .thenCancel()
                .verify()

        val request1 = mockWebServer.takeRequest()
        val request2 = mockWebServer.takeRequest()
        assertThat(request1.path)
                .isEqualTo("/_matrix/client/r0/sync?filter=someFilter&full_state=false&set_presence=online&since=nextBatch0&timeout=30000")
        assertThat(request2.path)
                .isEqualTo("/_matrix/client/r0/sync?filter=someFilter&full_state=false&set_presence=online&since=nextBatch1&timeout=30000")

        verifyOrder {
            syncBatchTokenService.batchToken = "nextBatch1"
            syncBatchTokenService.batchToken = "nextBatch2"
        }
    }
}