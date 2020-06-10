package net.folivo.matrix.restclient.api.rooms

import net.folivo.matrix.core.annotation.MatrixEvent
import net.folivo.matrix.core.api.MatrixClientException
import net.folivo.matrix.core.model.events.*
import net.folivo.matrix.core.model.events.m.room.CreateEvent
import net.folivo.matrix.core.model.events.m.room.MemberEvent
import net.folivo.matrix.core.model.events.m.room.PowerLevelsEvent
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

class RoomsApiClient(
        private val webClient: WebClient
) {

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-event-eventid">matrix spec</a>
     */
    fun getEvent(
            roomId: String,
            eventId: String,
            asUserId: String? = null
    ): Mono<Event<*>> {
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/event/{eventId}")
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(roomId, eventId)
                }
                .retrieve()
                .bodyToMono()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-state-eventtype-statekey">matrix spec</a>
     */
    inline fun <reified T : EventContent> getStateEvent(
            roomId: String,
            stateKey: String = "",
            eventType: String? = null,
            asUserId: String? = null
    ): Mono<T> {
        return getStateEvent(T::class.java, roomId, stateKey, eventType, asUserId)
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-state-eventtype-statekey">matrix spec</a>
     */
    fun <T : EventContent> getStateEvent(
            eventContentClass: Class<T>,
            roomId: String,
            stateKey: String = "",
            eventType: String? = null,
            asUserId: String? = null
    ): Mono<T> {
        val annotatedEventType = MergedAnnotations
                .from(eventContentClass, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY_AND_ENCLOSING_CLASSES)
                .get(MatrixEvent::class.java)
                .getValue("type", String::class.java)
                .orElse(eventType)
        if (annotatedEventType.isNullOrEmpty()) {
            return Mono.error(MatrixClientException("the eventContent should be an inner-class of your custom event to find eventType or else you must use the method parameter 'eventType'"))
        }
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/state/{eventType}/{stateKey}")
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(roomId, annotatedEventType, stateKey)
                }
                .retrieve()
                .bodyToMono(eventContentClass)
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-state">matrix spec</a>
     */
    fun getState(roomId: String, asUserId: String? = null): Flux<StateEvent<*, *>> {
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/state")
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(roomId)
                }
                .retrieve()
                .bodyToFlux()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-members">matrix spec</a>
     */
    fun getMembers(
            roomId: String,
            at: String? = null,
            membership: Membership? = null,
            notMembership: Membership? = null,
            asUserId: String? = null
    ): Flux<MemberEvent> {
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/members")
                        if (at != null) queryParam("at", at)
                        if (membership != null) queryParam("membership", membership.value)
                        if (notMembership != null) queryParam("not_membership", notMembership.value)
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(roomId)
                }
                .retrieve()
                .bodyToMono<GetMembersResponse>()
                .flatMapMany { Flux.fromIterable(it.chunk) }
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-joined-members">matrix spec</a>
     */
    fun getJoinedMembers(
            roomId: String,
            asUserId: String? = null
    ): Mono<GetJoinedMembersResponse> {
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/joined_members")
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(roomId)
                }
                .retrieve()
                .bodyToMono()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-messages">matrix spec</a>
     */
    fun getEvents(
            roomId: String,
            from: String,
            dir: Direction,
            to: String? = null,
            limit: Long = 10,
            filter: String? = null,
            asUserId: String? = null
    ): Mono<GetEventsResponse> {
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/messages")
                        queryParam("from", from)
                        if (to != null) queryParam("to", to)
                        queryParam("dir", dir.value)
                        queryParam("limit", limit.toString())
                        if (filter != null) queryParam("filter", filter)
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(roomId)
                }
                .retrieve()
                .bodyToMono()
    }


    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#put-matrix-client-r0-rooms-roomid-state-eventtype-statekey">matrix spec</a>
     */
    fun sendStateEvent( // TODO should handle resend by itself (maybe use reactors retry)
            roomId: String,
            eventContent: StateEventContent,
            stateKey: String,
            eventType: String? = null,
            asUserId: String? = null
    ): Mono<String> {
        val annotatedEventType = MergedAnnotations
                .from(eventContent::class.java, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY_AND_ENCLOSING_CLASSES)
                .get(MatrixEvent::class.java)
                .getValue("type", String::class.java)
                .orElse(eventType)
        if (annotatedEventType.isNullOrEmpty()) {
            return Mono.error(MatrixClientException("the eventContent should be an inner-class of your custom event to find eventType or else you must use the method parameter 'eventType'"))
        }

        return webClient
                .put().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/state/{eventType}/{stateKey}")
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(roomId, annotatedEventType, stateKey)
                }
                .bodyValue(eventContent)
                .retrieve()
                .bodyToMono<SendEventResponse>()
                .map { it.eventId }
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#put-matrix-client-r0-rooms-roomid-send-eventtype-txnid">matrix spec</a>
     */
    fun sendRoomEvent( // TODO should handle resend by itself (maybe use reactors retry)
            roomId: String,
            eventContent: RoomEventContent,
            eventType: String? = null,
            txnId: String = UUID.randomUUID().toString(),
            asUserId: String? = null
    ): Mono<String> {
        val annotatedEventType = MergedAnnotations
                .from(eventContent::class.java, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY_AND_ENCLOSING_CLASSES)
                .get(MatrixEvent::class.java)
                .getValue("type", String::class.java)
                .orElse(eventType)
        if (annotatedEventType.isNullOrEmpty()) {
            return Mono.error(MatrixClientException("the eventContent should be an inner-class of your custom event to find eventType or else you must use the method parameter 'eventType'"))
        }

        return webClient
                .put().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/send/{eventType}/{txnId}")
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(roomId, annotatedEventType, txnId)
                }
                .bodyValue(eventContent)
                .retrieve()
                .bodyToMono<SendEventResponse>()
                .map { it.eventId }
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#put-matrix-client-r0-rooms-roomid-redact-eventid-txnid">matrix spec</a>
     */
    fun sendRedactEvent( // TODO should handle resend by itself (maybe use reactors retry)
            roomId: String,
            eventId: String,
            reason: String,
            txnId: String = UUID.randomUUID().toString(),
            asUserId: String? = null
    ): Mono<String> {
        return webClient
                .put().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/redact/{eventId}/{txnId}")
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(roomId, eventId, txnId)
                }
                .bodyValue(mapOf("reason" to reason))
                .retrieve()
                .bodyToMono<SendEventResponse>()
                .map { it.eventId }
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-createroom">matrix spec</a>
     */
    fun createRoom(
            visibility: Visibility = Visibility.PRIVATE,
            roomAliasName: String? = null,
            name: String? = null,
            topic: String? = null,
            invite: Set<String>? = null,
            invite3Pid: Set<Invite3Pid>? = null,
            roomVersion: String? = null,
            creationContent: CreateEvent.CreateEventContent? = null,
            initialState: List<StateEvent<*, *>>? = null,
            preset: Preset? = null,
            isDirect: Boolean? = null,
            powerLevelContentOverride: PowerLevelsEvent.PowerLevelsEventContent? = null,
            asUserId: String? = null
    ): Mono<String> {
        return webClient
                .post().uri {
                    it.apply {
                        path("/r0/createRoom")
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build()
                }
                .bodyValue(
                        mapOf(
                                "visibility" to visibility.value,
                                "room_alias_name" to roomAliasName,
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
                .bodyToMono<CreateRoomResponse>()
                .map { it.roomId }
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#put-matrix-client-r0-directory-room-roomalias">matrix spec</a>
     */
    fun setRoomAlias(
            roomId: String,
            roomAlias: String,
            asUserId: String? = null
    ): Mono<Void> {
        return webClient
                .put().uri {
                    it.apply {
                        path("/r0/directory/room/{roomAlias}")
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(roomAlias)
                }
                .bodyValue(mapOf("room_id" to roomId))
                .retrieve()
                .bodyToMono()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-directory-room-roomalias">matrix spec</a>
     */
    fun getRoomAlias(
            roomAlias: String,
            asUserId: String? = null
    ): Mono<GetRoomAliasResponse> {
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/directory/room/{roomAlias}")
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(roomAlias)
                }
                .retrieve()
                .bodyToMono()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#delete-matrix-client-r0-directory-room-roomalias">matrix spec</a>
     */
    fun deleteRoomAlias(
            roomAlias: String,
            asUserId: String? = null
    ): Mono<Void> {
        return webClient
                .delete().uri {
                    it.apply {
                        path("/r0/directory/room/{roomAlias}")
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(roomAlias)
                }
                .retrieve()
                .bodyToMono()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-joined-rooms">matrix spec</a>
     */
    fun getJoinedRooms(asUserId: String? = null): Flux<String> {
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/joined_rooms")
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build()
                }
                .retrieve()
                .bodyToMono<GetJoinedRoomsResponse>()
                .flatMapMany { Flux.fromIterable(it.joinedRooms) }
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-rooms-roomid-invite">matrix spec</a>
     */
    fun inviteUser(
            roomId: String,
            userId: String,
            asUserId: String? = null
    ): Mono<Void> {
        return webClient
                .post().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/invite")
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(roomId)
                }
                .bodyValue(mapOf("user_id" to userId))
                .retrieve()
                .bodyToMono()
    }

// TODO does it work with empty servers? Maybe use also https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-rooms-roomid-join
    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-join-roomidoralias">matrix spec</a>
     */
    fun joinRoom(
            roomIdOrAlias: String,
            serverNames: Set<String>? = null,
            thirdPartySigned: ThirdPartySigned? = null,
            asUserId: String? = null
    ): Mono<String> {
        return webClient
                .post().uri {
                    it.apply {
                        path("/r0/join/{roomIdOrAlias}")
                        if (serverNames != null) queryParam("server_name", serverNames)
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(roomIdOrAlias)
                }
                .bodyValue(mapOf("third_party_signed" to thirdPartySigned))
                .retrieve()
                .bodyToMono<JoinRoomResponse>()
                .map { it.roomId }
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-rooms-roomid-leave">matrix spec</a>
     */
    fun leaveRoom(
            roomId: String,
            asUserId: String? = null
    ): Mono<Void> {
        return webClient
                .post().uri {
                    it.apply {
                        path("/r0/rooms/{roomId}/leave")
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(roomId)
                }
                .retrieve()
                .bodyToMono()
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