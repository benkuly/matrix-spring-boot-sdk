package net.folivo.matrix.restclient.api.rooms

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.awaitFirst
import net.folivo.matrix.core.annotation.MatrixEvent
import net.folivo.matrix.core.api.MatrixClientException
import net.folivo.matrix.core.model.MatrixId.*
import net.folivo.matrix.core.model.events.*
import net.folivo.matrix.core.model.events.m.room.CreateEvent
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.PowerLevelsEvent
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToFlow
import java.util.*

class RoomsApiClient(
        private val webClient: WebClient
) {

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-event-eventid">matrix spec</a>
     */
    suspend fun getEvent(
            roomId: RoomId,
            eventId: EventId,
            asUserId: UserId? = null
    ): Event<*> {
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/event/{eventId}")
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(roomId.full, eventId.full)
                }
                .retrieve()
                .awaitBody()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-state-eventtype-statekey">matrix spec</a>
     */
    suspend inline fun <reified T : EventContent> getStateEvent(
            roomId: RoomId,
            stateKey: String = "",
            eventType: String? = null,
            asUserId: UserId? = null
    ): T {
        return getStateEvent(T::class.java, roomId, stateKey, eventType, asUserId)
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-state-eventtype-statekey">matrix spec</a>
     */
    suspend fun <T : EventContent> getStateEvent(
            eventContentClass: Class<T>,
            roomId: RoomId,
            stateKey: String = "",
            eventType: String? = null,
            asUserId: UserId? = null
    ): T {
        val annotatedEventType = MergedAnnotations
                .from(eventContentClass, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY_AND_ENCLOSING_CLASSES)
                .get(MatrixEvent::class.java)
                .getValue("type", String::class.java)
                .orElse(eventType)
        if (annotatedEventType.isNullOrEmpty()) {
            throw MatrixClientException("the eventContent should be an inner-class of your custom event to find eventType or else you must use the method parameter 'eventType'")
        }
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/state/{eventType}/{stateKey}")
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(roomId.full, annotatedEventType, stateKey)
                }
                .retrieve()
                .bodyToMono(eventContentClass)
                .awaitFirst()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-state">matrix spec</a>
     */
    fun getState(roomId: RoomId, asUserId: UserId? = null): Flow<StateEvent<*, *>> {
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/state")
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(roomId.full)
                }
                .retrieve()
                .bodyToFlow()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-members">matrix spec</a>
     */
    fun getMembers(
            roomId: RoomId,
            at: String? = null,
            membership: Membership? = null,
            notMembership: Membership? = null,
            asUserId: UserId? = null
    ): Flow<MemberEvent> {
        return flow {
            webClient
                    .get().uri {
                        it.apply {
                            path("/r0/rooms/{roomId}/members")
                            if (at != null) queryParam("at", at)
                            if (membership != null) queryParam("membership", membership.value)
                            if (notMembership != null) queryParam("not_membership", notMembership.value)
                            if (asUserId != null) queryParam("user_id", asUserId.full)
                        }.build(roomId.full)
                    }
                    .retrieve()
                    .awaitBody<GetMembersResponse>()
                    .chunk
                    .forEach { emit(it) }
        }
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-joined-members">matrix spec</a>
     */
    suspend fun getJoinedMembers(
            roomId: RoomId,
            asUserId: UserId? = null
    ): GetJoinedMembersResponse {
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/joined_members")
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(roomId.full)
                }
                .retrieve()
                .awaitBody()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-messages">matrix spec</a>
     */
    suspend fun getEvents(
            roomId: RoomId,
            from: String,
            dir: Direction,
            to: String? = null,
            limit: Long = 10,
            filter: String? = null,
            asUserId: UserId? = null
    ): GetEventsResponse {
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/messages")
                        queryParam("from", from)
                        if (to != null) queryParam("to", to)
                        queryParam("dir", dir.value)
                        queryParam("limit", limit.toString())
                        if (filter != null) queryParam("filter", filter)
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(roomId.full)
                }
                .retrieve()
                .awaitBody()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#put-matrix-client-r0-rooms-roomid-state-eventtype-statekey">matrix spec</a>
     */
    suspend fun sendStateEvent( // TODO should handle resend by itself (maybe use reactors retry)
            roomId: RoomId,
            eventContent: StateEventContent,
            stateKey: String,
            eventType: String? = null,
            asUserId: UserId? = null
    ): EventId {
        val annotatedEventType = MergedAnnotations
                .from(eventContent::class.java, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY_AND_ENCLOSING_CLASSES)
                .get(MatrixEvent::class.java)
                .getValue("type", String::class.java)
                .orElse(eventType)
        if (annotatedEventType.isNullOrEmpty()) {
            throw MatrixClientException("the eventContent should be an inner-class of your custom event to find eventType or else you must use the method parameter 'eventType'")
        }

        return webClient
                .put().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/state/{eventType}/{stateKey}")
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(roomId.full, annotatedEventType, stateKey)
                }
                .bodyValue(eventContent)
                .retrieve()
                .awaitBody<SendEventResponse>()
                .eventId
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#put-matrix-client-r0-rooms-roomid-send-eventtype-txnid">matrix spec</a>
     */
    suspend fun sendRoomEvent( // TODO should handle resend by itself (maybe use reactors retry)
            roomId: RoomId,
            eventContent: RoomEventContent,
            eventType: String? = null,
            txnId: String = UUID.randomUUID().toString(),
            asUserId: UserId? = null
    ): EventId {
        val annotatedEventType = MergedAnnotations
                .from(eventContent::class.java, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY_AND_ENCLOSING_CLASSES)
                .get(MatrixEvent::class.java)
                .getValue("type", String::class.java)
                .orElse(eventType)
        if (annotatedEventType.isNullOrEmpty()) {
            throw MatrixClientException("the eventContent should be an inner-class of your custom event to find eventType or else you must use the method parameter 'eventType'")
        }

        return webClient
                .put().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/send/{eventType}/{txnId}")
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(roomId.full, annotatedEventType, txnId)
                }
                .bodyValue(eventContent)
                .retrieve()
                .awaitBody<SendEventResponse>()
                .eventId
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#put-matrix-client-r0-rooms-roomid-redact-eventid-txnid">matrix spec</a>
     */
    suspend fun sendRedactEvent( // TODO should handle resend by itself (maybe use reactors retry)
            roomId: RoomId,
            eventId: EventId,
            reason: String,
            txnId: String = UUID.randomUUID().toString(),
            asUserId: UserId? = null
    ): EventId {
        return webClient
                .put().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/redact/{eventId}/{txnId}")
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(roomId.full, eventId.full, txnId)
                }
                .bodyValue(mapOf("reason" to reason))
                .retrieve()
                .awaitBody<SendEventResponse>()
                .eventId
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-createroom">matrix spec</a>
     */
    suspend fun createRoom(
            visibility: Visibility = Visibility.PRIVATE,
            roomAliasId: RoomAliasId? = null,
            name: String? = null,
            topic: String? = null,
            invite: Set<UserId>? = null,
            invite3Pid: Set<Invite3Pid>? = null,
            roomVersion: String? = null,
            creationContent: CreateEvent.CreateEventContent? = null,
            initialState: List<StateEvent<*, *>>? = null,
            preset: Preset? = null,
            isDirect: Boolean? = null,
            powerLevelContentOverride: PowerLevelsEvent.PowerLevelsEventContent? = null,
            asUserId: UserId? = null
    ): RoomId {
        return webClient
                .post().uri {
                    it.apply {
                        path("/r0/createRoom")
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build()
                }
                .bodyValue(
                        mapOf(
                                "visibility" to visibility.value,
                                "room_alias_name" to roomAliasId?.full,
                                "name" to name,
                                "topic" to topic,
                                "invite" to invite,
                                "invite_3pid" to invite3Pid,
                                "room_version" to roomVersion,
                                "creation_content" to creationContent,
                                "initial_state" to initialState,
                                "preset" to preset?.value,
                                "is_direct" to isDirect,
                                "power_level_content_override" to powerLevelContentOverride
                        )
                )
                .retrieve()
                .awaitBody<CreateRoomResponse>()
                .roomId
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#put-matrix-client-r0-directory-room-roomalias">matrix spec</a>
     */
    suspend fun setRoomAlias(
            roomId: RoomId,
            roomAliasId: RoomAliasId,
            asUserId: UserId? = null
    ) {
        return webClient
                .put().uri {
                    it.apply {
                        path("/r0/directory/room/{roomAlias}")
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(roomAliasId.full)
                }
                .bodyValue(mapOf("room_id" to roomId.full))
                .retrieve()
                .awaitBody()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-directory-room-roomalias">matrix spec</a>
     */
    suspend fun getRoomAlias(
            roomAliasId: RoomAliasId,
            asUserId: UserId? = null
    ): GetRoomAliasResponse {
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/directory/room/{roomAlias}")
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(roomAliasId.full)
                }
                .retrieve()
                .awaitBody()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#delete-matrix-client-r0-directory-room-roomalias">matrix spec</a>
     */
    suspend fun deleteRoomAlias(
            roomAliasId: RoomAliasId,
            asUserId: UserId? = null
    ) {
        return webClient
                .delete().uri {
                    it.apply {
                        path("/r0/directory/room/{roomAlias}")
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(roomAliasId.full)
                }
                .retrieve()
                .awaitBody()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-joined-rooms">matrix spec</a>
     */
    fun getJoinedRooms(asUserId: UserId? = null): Flow<RoomId> {
        return flow {
            webClient
                    .get().uri {
                        it.apply {
                            path("/r0/joined_rooms")
                            if (asUserId != null) queryParam("user_id", asUserId.full)
                        }.build()
                    }
                    .retrieve()
                    .awaitBody<GetJoinedRoomsResponse>()
                    .joinedRooms
                    .forEach { emit(it) }
        }
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-rooms-roomid-invite">matrix spec</a>
     */
    suspend fun inviteUser(
            roomId: RoomId,
            userId: UserId,
            asUserId: UserId? = null
    ) {
        return webClient
                .post().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/invite")
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(roomId.full)
                }
                .bodyValue(mapOf("user_id" to userId.full))
                .retrieve()
                .awaitBody()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-join-roomidoralias">matrix spec</a>
     */
    suspend fun joinRoom(
            roomId: RoomId,
            serverNames: Set<String>? = null,
            thirdPartySigned: ThirdPartySigned? = null,
            asUserId: UserId? = null
    ): RoomId {
        return webClient
                .post().uri {
                    it.apply {
                        path("/r0/join/{roomIdOrAlias}")
                        if (serverNames != null) queryParam("server_name", serverNames)
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(roomId.full)
                }
                .bodyValue(mapOf("third_party_signed" to thirdPartySigned))
                .retrieve()
                .awaitBody<JoinRoomResponse>()
                .roomId
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-join-roomidoralias">matrix spec</a>
     */
    suspend fun joinRoom(
            roomAliasId: RoomAliasId,
            serverNames: Set<String>? = null,
            thirdPartySigned: ThirdPartySigned? = null,
            asUserId: UserId? = null
    ): RoomId {
        return webClient
                .post().uri {
                    it.apply {
                        path("/r0/join/{roomIdOrAlias}")
                        if (serverNames != null) queryParam("server_name", serverNames)
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(roomAliasId.full)
                }
                .bodyValue(mapOf("third_party_signed" to thirdPartySigned))
                .retrieve()
                .awaitBody<JoinRoomResponse>()
                .roomId
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-rooms-roomid-leave">matrix spec</a>
     */
    suspend fun leaveRoom(
            roomId: RoomId,
            asUserId: UserId? = null
    ) {
        return webClient
                .post().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/leave")
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(roomId.full)
                }
                .retrieve()
                .awaitBody()
    }

//    /**
//     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-rooms-roomid-forget">matrix spec</a>
//     */
//    fun forgetRoom() {
//        // TODO implement
//    }

//    /**
//     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-rooms-roomid-kick">matrix spec</a>
//     */
//    fun kickUser() {
//        // TODO implement
//    }

//    /**
//     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-rooms-roomid-ban">matrix spec</a>
//     */
//    fun banUser() {
//        // TODO implement
//    }

//    /**
//     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-rooms-roomid-unban">matrix spec</a>
//     */
//    fun unbanUser() {
//        // TODO implement
//    }

//    /**
//     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-directory-list-room-roomid">matrix spec</a>
//     */
//    fun getVisibility() {
//        // TODO implement
//    }

//    /**
//     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#put-matrix-client-r0-directory-list-room-roomid">matrix spec</a>
//     */
//    fun setVisibility() {
//        // TODO implement
//    }

//    /**
//     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-publicrooms">matrix spec</a>
//     */
//    fun getPublicRooms() {
//        // TODO implement
//    }
}