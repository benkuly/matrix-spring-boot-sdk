package net.folivo.matrix.restclient.api.sync

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.junit5.MockKExtension
import io.mockk.just
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.sync.Presence.ONLINE
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
import org.springframework.http.HttpMethod.GET
import org.springframework.http.MediaType

@SpringBootTest(properties = ["matrix.client.homeServer.port=5003"])
@ExtendWith(MockKExtension::class)
class SyncApiClientTest {
    @Autowired
    lateinit var matrixClient: MatrixClient

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
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

        val result = runBlocking {
            matrixClient.syncApi.syncOnce(
                    filter = "someFilter",
                    fullState = true,
                    setPresence = ONLINE,
                    since = "someSince",
                    timeout = 1234
            )
        }

        assertThat(result).isEqualTo(response)

        val request = mockWebServer.takeRequest()
        assertThat(request.path)
                .isEqualTo("/_matrix/client/r0/sync?filter=someFilter&full_state=true&set_presence=online&since=someSince&timeout=1234")
        assertThat(request.method).isEqualTo(GET.toString())
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

        coEvery { syncBatchTokenService.getBatchToken() }
                .returnsMany(null, "nextBatch1")
        coEvery { syncBatchTokenService.setBatchToken(any()) } just Runs

        val result = matrixClient.syncApi.syncLoop(
                filter = "someFilter",
                setPresence = ONLINE
        )

        val responses = runBlocking { result.take(2).toList() }

        assertThat(responses[0]).isEqualTo(response1)
        assertThat(responses[1]).isEqualTo(response2)


        val request1 = mockWebServer.takeRequest()
        val request2 = mockWebServer.takeRequest()
        assertThat(request1.path)
                .isEqualTo("/_matrix/client/r0/sync?filter=someFilter&full_state=false&set_presence=online&timeout=30000")
        assertThat(request2.path)
                .isEqualTo("/_matrix/client/r0/sync?filter=someFilter&full_state=false&set_presence=online&since=nextBatch1&timeout=30000")

        coVerifyOrder {
            syncBatchTokenService.setBatchToken("nextBatch1")
            syncBatchTokenService.setBatchToken("nextBatch2")
        }
    }

    @Test
    fun `should retry syncLoop on errors`() {
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
                        .setResponseCode(404)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(""))
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response2))
        )

        coEvery { syncBatchTokenService.getBatchToken() }
                .returnsMany(null, "nextBatch1")
        coEvery { syncBatchTokenService.setBatchToken(any()) } just Runs

        val result = matrixClient.syncApi.syncLoop(
                filter = "someFilter",
                setPresence = ONLINE
        )

        val responses = runBlocking { result.take(2).toList() }

        assertThat(responses[0]).isEqualTo(response1)
        assertThat(responses[1]).isEqualTo(response2)


        val request1 = mockWebServer.takeRequest()
        val request2 = mockWebServer.takeRequest()
        assertThat(request1.path)
                .isEqualTo("/_matrix/client/r0/sync?filter=someFilter&full_state=false&set_presence=online&timeout=30000")
        assertThat(request2.path)
                .isEqualTo("/_matrix/client/r0/sync?filter=someFilter&full_state=false&set_presence=online&since=nextBatch1&timeout=30000")

        coVerifyOrder {
            syncBatchTokenService.setBatchToken("nextBatch1")
            syncBatchTokenService.setBatchToken("nextBatch2")
        }
    }

}