package net.folivo.matrix.restclient.api.rooms

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.folivo.matrix.common.api.MatrixClientException
import net.folivo.matrix.common.model.events.RoomEventContent
import net.folivo.matrix.common.model.events.StateEvent
import net.folivo.matrix.common.model.events.StateEventContent
import net.folivo.matrix.common.model.events.m.room.AliasesEvent
import net.folivo.matrix.common.model.events.m.room.MemberEvent
import net.folivo.matrix.common.model.events.m.room.message.TextMessageEventContent
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
import reactor.test.StepVerifier

@SpringBootTest(properties = ["matrix.homeServer.port=5001"])
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
        val response = AliasesEvent(
                id = "someEventId",
                roomId = "someRoomId",
                unsigned = StateEvent.UnsignedData(),
                originTimestamp = 1234,
                sender = "sender",
                stateKey = "stateKey",
                content = AliasesEvent.AliasesEventContent()
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = matrixClient.roomsApi.getEvent("someRoomId", "someEventId").block()

        if (result !is AliasesEvent) {
            fail<Unit>("result should be of type ${AliasesEvent::class}")
        }

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/someRoomId/event/someEventId")
        assertThat(request.method).isEqualTo(HttpMethod.GET.toString())
    }

    @Test
    fun `should get some state event`() {
        val response = AliasesEvent.AliasesEventContent(listOf("#alias:example.com"))
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = matrixClient.roomsApi.getStateEvent<AliasesEvent.AliasesEventContent>(
                roomId = "someRoomId",
                stateKey = "someStateKey"
        ).block()

        if (result !is AliasesEvent.AliasesEventContent) {
            fail<Unit>("result should be of type ${AliasesEvent.AliasesEventContent::class}")
        }

        val request = mockWebServer.takeRequest()
        assertThat(request.path)
                .isEqualTo("/_matrix/client/r0/rooms/someRoomId/state/m.room.aliases/someStateKey")
        assertThat(request.method).isEqualTo(HttpMethod.GET.toString())
    }

    @Test
    fun `should get complete state`() {
        val response = listOf(
                AliasesEvent(
                        id = "someEventId1",
                        roomId = "someRoomId",
                        unsigned = StateEvent.UnsignedData(),
                        originTimestamp = 12341,
                        sender = "sender",
                        stateKey = "stateKey1",
                        content = AliasesEvent.AliasesEventContent()
                ),
                MemberEvent(
                        id = "someEventId2",
                        roomId = "someRoomId",
                        unsigned = MemberEvent.MemberUnsignedData(),
                        originTimestamp = 12342,
                        sender = "sender",
                        stateKey = "stateKey2",
                        content = MemberEvent.MemberEventContent(membership = MemberEvent.MemberEventContent.Membership.INVITE)
                )
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = matrixClient.roomsApi.getState("someRoomId").collectList().block()

        assertThat(result).isNotNull
        assertThat(result).hasSize(2)
        if (result !== null) {
            if (result[0] !is AliasesEvent) {
                fail<Unit>("result should be of type ${AliasesEvent::class}")
            }
            if (result[1] !is MemberEvent) {
                fail<Unit>("result should be of type ${MemberEvent::class}")
            }
        }

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/someRoomId/state")
        assertThat(request.method).isEqualTo(HttpMethod.GET.toString())
    }

    @Test
    fun `should get members`() {
        val response = GetMembersResponse(
                listOf(
                        MemberEvent(
                                id = "someEventId1",
                                roomId = "someRoomId",
                                unsigned = MemberEvent.MemberUnsignedData(),
                                originTimestamp = 12341,
                                sender = "sender",
                                stateKey = "stateKey1",
                                content = MemberEvent.MemberEventContent(membership = MemberEvent.MemberEventContent.Membership.INVITE)
                        ),
                        MemberEvent(
                                id = "someEventId2",
                                roomId = "someRoomId",
                                unsigned = MemberEvent.MemberUnsignedData(),
                                originTimestamp = 12342,
                                sender = "sender",
                                stateKey = "stateKey2",
                                content = MemberEvent.MemberEventContent(membership = MemberEvent.MemberEventContent.Membership.INVITE)
                        )
                )
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = matrixClient.roomsApi.getMembers(
                roomId = "someRoomId",
                at = "someAt",
                membership = Membership.JOIN
        ).collectList().block()

        assertThat(result).isNotNull
        assertThat(result).hasSize(2)
        if (result !== null && result[0] !is MemberEvent && result[1] !is MemberEvent) {
            fail<Unit>("result should be of type ${MemberEvent::class}")
        }

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/someRoomId/members?at=someAt&membership=join")
        assertThat(request.method).isEqualTo(HttpMethod.GET.toString())
    }

    @Test
    fun `should get joined members`() {
        val response = GetJoinedMembersResponse(
                joined = mapOf(
                        "@user1:example.com" to GetJoinedMembersResponse.RoomMember("Unicorn"),
                        "@user2:example.com" to GetJoinedMembersResponse.RoomMember("Dino")
                )
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = matrixClient.roomsApi.getJoinedMembers("someRoomId").block()

        assertThat(result).isEqualTo(response)

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/someRoomId/joined_members")
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

        val result = matrixClient.roomsApi.getEvents(
                roomId = "someRoomId",
                from = "from",
                dir = Direction.FORWARD
        ).block()

        assertThat(result).isEqualTo(response)

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/someRoomId/messages?from=from&dir=f&limit=10")
        assertThat(request.method).isEqualTo(HttpMethod.GET.toString())
    }

    @Test
    fun `should send state event`() {
        val response = SendEventResponse("someEventId")
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val eventContent = AliasesEvent.AliasesEventContent(listOf("#someAlias:example.com"))
        val result = matrixClient.roomsApi.sendStateEvent(
                roomId = "someRoomId",
                eventContent = eventContent,
                stateKey = "someStateKey"
        ).block()

        assertThat(result).isEqualTo("someEventId")

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/someRoomId/state/m.room.aliases/someStateKey")
        assertThat(request.body.readUtf8()).isEqualTo(objectMapper.writeValueAsString(eventContent))
        assertThat(request.method).isEqualTo(HttpMethod.PUT.toString())
    }

    @Test
    fun `should have error when no eventType found on sending state event`() {
        val response = SendEventResponse("someEventId")
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val eventContent = object : StateEventContent {
            val banana: String = "yeah"
        }
        val result = matrixClient.roomsApi.sendStateEvent(
                roomId = "someRoomId",
                eventContent = eventContent,
                stateKey = "someStateKey"
        )

        StepVerifier.create(result).consumeErrorWith {
            if (it !is MatrixClientException) {
                fail<Unit>("error should be of type ${MatrixClientException::class} but was ${it::class}")
            }
        }.verify()
    }

    @Test
    fun `should send room event`() {
        val response = SendEventResponse("someEventId")
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val eventContent = TextMessageEventContent("someBody")
        val result = matrixClient.roomsApi.sendRoomEvent(
                roomId = "someRoomId",
                eventContent = eventContent,
                txnId = "someTxnId"
        ).block()

        assertThat(result).isEqualTo("someEventId")

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/someRoomId/send/m.room.message/someTxnId")
        assertThat(request.body.readUtf8()).isEqualTo(objectMapper.writeValueAsString(eventContent))
        assertThat(request.method).isEqualTo(HttpMethod.PUT.toString())
    }

    @Test
    fun `should have error when no eventType found on sending room event`() {
        val response = SendEventResponse("someEventId")
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val eventContent = object : RoomEventContent {
            val banana: String = "yeah"
        }
        val result = matrixClient.roomsApi.sendRoomEvent(
                roomId = "someRoomId",
                eventContent = eventContent
        )

        StepVerifier.create(result).consumeErrorWith {
            if (it !is MatrixClientException) {
                fail<Unit>("error should be of type ${MatrixClientException::class} but was ${it::class}")
            }
        }.verify()
    }

    @Test
    fun `should send redact event`() {
        val response = SendEventResponse("someEventId")
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = matrixClient.roomsApi.sendRedactEvent(
                roomId = "someRoomId",
                eventId = "someEventIdToRedact",
                reason = "someReason",
                txnId = "someTxnId"
        ).block()

        assertThat(result).isEqualTo("someEventId")

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/someRoomId/redact/someEventIdToRedact/someTxnId")
        assertThat(request.body.readUtf8()).isEqualTo("""{"reason":"someReason"}""")
        assertThat(request.method).isEqualTo(HttpMethod.PUT.toString())
    }

    @Test
    fun `should create room`() {
        val response = CreateRoomResponse("someRoomId")
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = matrixClient.roomsApi.createRoom(
                visibility = Visibility.PRIVATE,
                invite = listOf("@user1:example.com"),
                isDirect = true,
                name = "someRoomName",
                invite3Pid = listOf(Invite3Pid("identityServer", "token", "email", "user2@example.org"))
        ).block()

        assertThat(result).isEqualTo("someRoomId")

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/createRoom")

        val expectedRequest = """
            {
                "visibility" : "private",
                "name" : "someRoomName",
                "invite" : ["@user1:example.com"],
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

        val result = matrixClient.roomsApi.setRoomAlias(
                roomId = "someRoomId",
                roomAlias = "#unicorns:example.org"
        ).block()

        assertThat(result).isNull()

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/directory/room/%23unicorns%3Aexample.org")
        assertThat(request.body.readUtf8()).isEqualTo("""{"room_id":"someRoomId"}""")
        assertThat(request.method).isEqualTo(HttpMethod.PUT.toString())
    }

    @Test
    fun `should get room alias`() {
        val response = GetRoomAliasResponse(
                roomId = "someRoomId",
                servers = listOf("server1", "server2")
        )
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = matrixClient.roomsApi.getRoomAlias("#unicorns:example.org").block()

        assertThat(result).isEqualTo(response)

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/directory/room/%23unicorns%3Aexample.org")
        assertThat(request.method).isEqualTo(HttpMethod.GET.toString())
    }

    @Test
    fun `should delete room alias`() {
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody("{}")
        )

        val result = matrixClient.roomsApi.deleteRoomAlias("#unicorns:example.org").block()

        assertThat(result).isNull()

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/directory/room/%23unicorns%3Aexample.org")
        assertThat(request.method).isEqualTo(HttpMethod.DELETE.toString())
    }

    @Test
    fun `should get joined rooms`() {
        val response = GetJoinedRoomsResponse(listOf("room1", "room2"))
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = matrixClient.roomsApi.getJoinedRooms().collectList().block()

        assertThat(result).containsOnly("room1", "room2")

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

        val result = matrixClient.roomsApi.inviteUser("someRoomId", "someUserId").block()

        assertThat(result).isNull()

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/rooms/someRoomId/invite")
        assertThat(request.body.readUtf8()).isEqualTo("""{"user_id":"someUserId"}""")
        assertThat(request.method).isEqualTo(HttpMethod.POST.toString())
    }

    @Test
    fun `should join room`() {
        val response = JoinRoomResponse("someRoomId")
        mockWebServer.enqueue(
                MockResponse()
                        .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(response))
        )

        val result = matrixClient.roomsApi.joinRoom(
                roomIdOrAlias = "someRoomId",
                serverNames = listOf("someServer"),
                thirdPartySigned = ThirdPartySigned(
                        sender = "@alice:example.org",
                        mxid = "@bob:example.org",
                        token = "someToken",
                        signatures = mapOf(
                                "example.org" to
                                        mapOf("ed25519:0" to "some9signature")
                        )
                )
        ).block()

        val expectedRequest = """
            {
              "third_party_signed": {
                "sender": "@alice:example.org",
                "mxid": "@bob:example.org",
                "token": "someToken",
                "signatures": {
                  "example.org": {
                    "ed25519:0": "some9signature"
                  }
                }
              }
            }

        """.trimIndent()

        assertThat(result).isEqualTo("someRoomId")

        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/_matrix/client/r0/join/someRoomId?server_name=someServer")
        assertThat(request.body.readUtf8()).isEqualTo(
                objectMapper.readValue<JsonNode>(expectedRequest).toString()
        )
        assertThat(request.method).isEqualTo(HttpMethod.POST.toString())
    }
}