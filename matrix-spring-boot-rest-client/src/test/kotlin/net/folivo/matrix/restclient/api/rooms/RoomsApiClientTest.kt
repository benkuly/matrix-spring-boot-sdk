package net.folivo.matrix.restclient.api.rooms

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.runBlocking
import net.folivo.matrix.core.api.MatrixClientException
import net.folivo.matrix.core.model.MatrixId.*
import net.folivo.matrix.core.model.events.RoomEventContent
import net.folivo.matrix.core.model.events.StateEvent
import net.folivo.matrix.core.model.events.StateEventContent
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.NameEvent
import net.folivo.matrix.core.model.events.m.room.NameEvent.NameEventContent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import net.folivo.matrix.restclient.MatrixClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

@SpringBootTest(properties = ["matrix.client.homeServer.port=5001"])
class RoomsApiClientTest {

    @Autowired
    lateinit var matrixClient: MatrixClient

    @Autowired
    lateinit var objectMapper: ObjectMapper

    val mockWebServer = MockWebServer()

    @BeforeEach
    fun setup() {
        mockWebServer.start(5001)
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `should get some room event`() {
        val response = NameEvent(
                id = EventId("event", "server"),
                roomId = RoomId("room", "server"),
                unsigned = StateEvent.UnsignedData(),
                originTimestamp = 1234,
                sender = UserId("sender", "server"),
                content = NameEventContent()
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = runBlocking {
            matrixClient.roomsApi.getEvent(
                    RoomId("room", "server"),
                    EventId("event", "server")
            )
        }

        if (result !is NameEvent) {
            fail<Unit>("result should be of type ${NameEvent::class}")
        }

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/%21room%3Aserver/event/%24event%3Aserver")
        assertThat(request.method).isEqualTo(HttpMethod.GET.toString())
    }

    @Test
    fun `should get some state event`() {
        val response = NameEventContent("name")
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        runBlocking {
            matrixClient.roomsApi.getStateEvent<NameEventContent>(
                    roomId = RoomId("room", "server"),
            )
        }

        val request = mockWebServer.takeRequest()
        assertThat(request.path)
                .isEqualTo("/_matrix/client/r0/rooms/%21room%3Aserver/state/m.room.name/")
        assertThat(request.method).isEqualTo(HttpMethod.GET.toString())
    }

    @Test
    fun `should get complete state`() {
        val response = listOf(
                NameEvent(
                        id = EventId("event1", "server"),
                        roomId = RoomId("room", "server"),
                        unsigned = StateEvent.UnsignedData(),
                        originTimestamp = 12341,
                        sender = UserId("sender", "server"),
                        content = NameEventContent()
                ),
                MemberEvent(
                        id = EventId("event2", "server"),
                        roomId = RoomId("room", "server"),
                        unsigned = MemberEvent.MemberUnsignedData(),
                        originTimestamp = 12342,
                        sender = UserId("sender", "server"),
                        relatedUser = UserId("user", "server"),
                        content = MemberEvent.MemberEventContent(membership = MemberEvent.MemberEventContent.Membership.INVITE)
                )
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = runBlocking { matrixClient.roomsApi.getState(RoomId("room", "server")).toList() }

        assertThat(result).isNotNull
        assertThat(result).hasSize(2)
        if (result[0] !is NameEvent) {
            fail<Unit>("result should be of type ${NameEvent::class}")
        }
        if (result[1] !is MemberEvent) {
            fail<Unit>("result should be of type ${MemberEvent::class}")
        }

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/%21room%3Aserver/state")
        assertThat(request.method).isEqualTo(HttpMethod.GET.toString())
    }

    @Test
    fun `should get members`() {
        val response = GetMembersResponse(
                listOf(
                        MemberEvent(
                                id = EventId("event1", "server"),
                                roomId = RoomId("room", "server"),
                                unsigned = MemberEvent.MemberUnsignedData(),
                                originTimestamp = 12341,
                                sender = UserId("sender", "server"),
                                relatedUser = UserId("user1", "server"),
                                content = MemberEvent.MemberEventContent(membership = MemberEvent.MemberEventContent.Membership.INVITE)
                        ),
                        MemberEvent(
                                id = EventId("event2", "server"),
                                roomId = RoomId("room", "server"),
                                unsigned = MemberEvent.MemberUnsignedData(),
                                originTimestamp = 12342,
                                sender = UserId("sender", "server"),
                                relatedUser = UserId("user2", "server"),
                                content = MemberEvent.MemberEventContent(membership = MemberEvent.MemberEventContent.Membership.INVITE)
                        )
                )
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = runBlocking {
            matrixClient.roomsApi.getMembers(
                    roomId = RoomId("room", "server"),
                    at = "someAt",
                    membership = Membership.JOIN
            ).toList()
        }

        assertThat(result).isNotNull
        assertThat(result).hasSize(2)

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/%21room%3Aserver/members?at=someAt&membership=join")
        assertThat(request.method).isEqualTo(HttpMethod.GET.toString())
    }

    @Test
    fun `should get joined members`() {
        val response = GetJoinedMembersResponse(
                joined = mapOf(
                        UserId("user1", "server") to GetJoinedMembersResponse.RoomMember("Unicorn"),
                        UserId("user2", "server") to GetJoinedMembersResponse.RoomMember("Dino")
                )
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        println(objectMapper.writeValueAsString(response))

        val result = runBlocking { matrixClient.roomsApi.getJoinedMembers(RoomId("room", "server")) }

        assertThat(result).isEqualTo(response)

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/%21room%3Aserver/joined_members")
        assertThat(request.method).isEqualTo(HttpMethod.GET.toString())
    }

    @Test
    fun `should get events`() {
        val response = GetEventsResponse(
                start = "start",
                end = "end",
                chunk = listOf(),
                state = listOf()
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = runBlocking {
            matrixClient.roomsApi.getEvents(
                    roomId = RoomId("room", "server"),
                    from = "from",
                    dir = Direction.FORWARD
            )
        }

        assertThat(result).isEqualTo(response)

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/%21room%3Aserver/messages?from=from&dir=f&limit=10")
        assertThat(request.method).isEqualTo(HttpMethod.GET.toString())
    }

    @Test
    fun `should send state event`() {
        val response = SendEventResponse(EventId("event", "server"))
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val eventContent = NameEventContent("name")
        val result = runBlocking {
            matrixClient.roomsApi.sendStateEvent(
                    roomId = RoomId("room", "server"),
                    eventContent = eventContent,
                    stateKey = "someStateKey"
            )
        }

        result.shouldBe(EventId("event", "server"))

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/%21room%3Aserver/state/m.room.name/someStateKey")
        assertThat(request.body.readUtf8()).isEqualTo(objectMapper.writeValueAsString(eventContent))
        assertThat(request.method).isEqualTo(HttpMethod.PUT.toString())
    }

    @Test
    fun `should have error when no eventType found on sending state event`() {
        val response = SendEventResponse(EventId("event", "server"))
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val eventContent = object : StateEventContent {
            val banana: String = "yeah"
        }

        try {
            runBlocking {
                matrixClient.roomsApi.sendStateEvent(
                        roomId = RoomId("room", "server"),
                        eventContent = eventContent,
                        stateKey = "someStateKey"
                )
            }
            fail<Unit>("error has error")
        } catch (error: Throwable) {
            if (error !is MatrixClientException) {
                fail<Unit>("error should be of type ${MatrixClientException::class} but was ${error::class}")
            }
        }
    }

    @Test
    fun `should send room event`() {
        val response = SendEventResponse(EventId("event", "server"))
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val eventContent = TextMessageEventContent("someBody")
        val result = runBlocking {
            matrixClient.roomsApi.sendRoomEvent(
                    roomId = RoomId("room", "server"),
                    eventContent = eventContent,
                    txnId = "someTxnId"
            )
        }

        result.shouldBe(EventId("event", "server"))

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/%21room%3Aserver/send/m.room.message/someTxnId")
        assertThat(request.body.readUtf8()).isEqualTo(objectMapper.writeValueAsString(eventContent))
        assertThat(request.method).isEqualTo(HttpMethod.PUT.toString())
    }

    @Test
    fun `should have error when no eventType found on sending room event`() {
        val response = SendEventResponse(EventId("event", "server"))
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val eventContent = object : RoomEventContent {
            val banana: String = "yeah"
        }
        try {
            runBlocking {
                matrixClient.roomsApi.sendRoomEvent(
                        roomId = RoomId("room", "server"),
                        eventContent = eventContent
                )
            }
            fail<Unit>("error has error")
        } catch (error: Throwable) {
            if (error !is MatrixClientException) {
                fail<Unit>("error should be of type ${MatrixClientException::class} but was ${error::class}")
            }
        }
    }

    @Test
    fun `should send redact event`() {
        val response = SendEventResponse(EventId("event", "server"))
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = runBlocking {
            matrixClient.roomsApi.sendRedactEvent(
                    roomId = RoomId("room", "server"),
                    eventId = EventId("eventToRedact", "server"),
                    reason = "someReason",
                    txnId = "someTxnId"
            )
        }

        result.shouldBe(EventId("event", "server"))

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/%21room%3Aserver/redact/%24eventToRedact%3Aserver/someTxnId")
        assertThat(request.body.readUtf8()).isEqualTo("""{"reason":"someReason"}""")
        assertThat(request.method).isEqualTo(HttpMethod.PUT.toString())
    }

    @Test
    fun `should create room`() {
        val response = CreateRoomResponse(RoomId("room", "server"))
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = runBlocking {
            matrixClient.roomsApi.createRoom(
                    visibility = Visibility.PRIVATE,
                    invite = setOf(UserId("user1", "server")),
                    isDirect = true,
                    name = "someRoomName",
                    invite3Pid = setOf(Invite3Pid("identityServer", "token", "email", "user2@example.org"))
            )
        }

        result.shouldBe(RoomId("room", "server"))

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/createRoom")

        val expectedRequest = """
            {
                "visibility" : "private",
                "name" : "someRoomName",
                "invite" : ["@user1:server"],
                "invite_3pid" : [{
                    "id_server" : "identityServer",
                    "id_access_token" : "token",
                    "medium" : "email",
                    "address" : "user2@example.org"
                }],
                "is_direct" : true
            }
        """.trimIndent()
        assertThat(request.body.readUtf8()).isEqualTo(
                objectMapper.readValue<JsonNode>(expectedRequest).toString()
        )
        assertThat(request.method).isEqualTo(HttpMethod.POST.toString())
    }

    @Test
    fun `should set room alias`() {
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody("{}")
        )

        runBlocking {
            matrixClient.roomsApi.setRoomAlias(
                    roomId = RoomId("room", "server"),
                    roomAliasId = RoomAliasId("unicorns", "server")
            )
        }

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/directory/room/%23unicorns%3Aserver")
        assertThat(request.body.readUtf8()).isEqualTo("""{"room_id":"!room:server"}""")
        assertThat(request.method).isEqualTo(HttpMethod.PUT.toString())
    }

    @Test
    fun `should get room alias`() {
        val response = GetRoomAliasResponse(
                roomId = RoomId("room", "server"),
                servers = listOf("server1", "server2")
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = runBlocking { matrixClient.roomsApi.getRoomAlias(RoomAliasId("unicorns", "server")) }

        result.shouldBe(response)

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/directory/room/%23unicorns%3Aserver")
        assertThat(request.method).isEqualTo(HttpMethod.GET.toString())
    }

    @Test
    fun `should delete room alias`() {
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody("{}")
        )

        runBlocking { matrixClient.roomsApi.deleteRoomAlias(RoomAliasId("unicorns", "server")) }

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/directory/room/%23unicorns%3Aserver")
        assertThat(request.method).isEqualTo(HttpMethod.DELETE.toString())
    }

    @Test
    fun `should get joined rooms`() {
        val response = GetJoinedRoomsResponse(
                setOf(
                        RoomId("room1", "server"), RoomId("room2", "server")
                )
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = runBlocking { matrixClient.roomsApi.getJoinedRooms().toSet() }

        result.shouldContainExactlyInAnyOrder(RoomId("room1", "server"), RoomId("room2", "server"))

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/joined_rooms")
        assertThat(request.method).isEqualTo(HttpMethod.GET.toString())
    }

    @Test
    fun `should invite user`() {
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody("{}")
        )

        runBlocking { matrixClient.roomsApi.inviteUser(RoomId("room", "server"), UserId("user", "server")) }

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/%21room%3Aserver/invite")
        assertThat(request.body.readUtf8()).isEqualTo("""{"user_id":"@user:server"}""")
        assertThat(request.method).isEqualTo(HttpMethod.POST.toString())
    }

    @Test
    fun `should join room by room id`() {
        val response = JoinRoomResponse(RoomId("room", "server"))
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = runBlocking {
            matrixClient.roomsApi.joinRoom(
                    roomId = RoomId("room", "server"),
                    serverNames = setOf("server"),
                    thirdPartySigned = ThirdPartySigned(
                            sender = UserId("alice", "server"),
                            mxid = UserId("bob", "server"),
                            token = "someToken",
                            signatures = mapOf(
                                    "example.org" to
                                            mapOf("ed25519:0" to "some9signature")
                            )
                    )
            )
        }

        val expectedRequest = """
            {
              "third_party_signed": {
                "sender": "@alice:server",
                "mxid": "@bob:server",
                "token": "someToken",
                "signatures": {
                  "example.org": {
                    "ed25519:0": "some9signature"
                  }
                }
              }
            }

        """.trimIndent()

        result.shouldBe(RoomId("room", "server"))

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/join/%21room%3Aserver?server_name=server")
        assertThat(request.body.readUtf8()).isEqualTo(
                objectMapper.readValue<JsonNode>(expectedRequest).toString()
        )
        assertThat(request.method).isEqualTo(HttpMethod.POST.toString())
    }

    @Test
    fun `should join room by room alias`() {
        val response = JoinRoomResponse(RoomId("room", "server"))
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = runBlocking {
            matrixClient.roomsApi.joinRoom(
                    roomAliasId = RoomAliasId("alias", "server"),
                    serverNames = setOf("server"),
                    thirdPartySigned = ThirdPartySigned(
                            sender = UserId("alice", "server"),
                            mxid = UserId("bob", "server"),
                            token = "someToken",
                            signatures = mapOf(
                                    "example.org" to
                                            mapOf("ed25519:0" to "some9signature")
                            )
                    )
            )
        }

        val expectedRequest = """
            {
              "third_party_signed": {
                "sender": "@alice:server",
                "mxid": "@bob:server",
                "token": "someToken",
                "signatures": {
                  "example.org": {
                    "ed25519:0": "some9signature"
                  }
                }
              }
            }

        """.trimIndent()

        result.shouldBe(RoomId("room", "server"))

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/join/%23alias%3Aserver?server_name=server")
        assertThat(request.body.readUtf8()).isEqualTo(
                objectMapper.readValue<JsonNode>(expectedRequest).toString()
        )
        assertThat(request.method).isEqualTo(HttpMethod.POST.toString())
    }

    @Test
    fun `should leave room`() {
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody("{}")
        )

        runBlocking { matrixClient.roomsApi.leaveRoom(RoomId("room", "server")) }

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/%21room%3Aserver/leave")
        assertThat(request.method).isEqualTo(HttpMethod.POST.toString())
    }
}