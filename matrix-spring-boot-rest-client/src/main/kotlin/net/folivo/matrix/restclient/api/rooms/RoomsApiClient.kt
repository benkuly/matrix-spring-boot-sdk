package net.folivo.matrix.restclient.api.rooms

import net.folivo.matrix.restclient.annotation.MatrixEvent
import net.folivo.matrix.restclient.api.MatrixClientException
import net.folivo.matrix.restclient.model.events.*
import net.folivo.matrix.restclient.model.events.m.room.CreateEvent
import net.folivo.matrix.restclient.model.events.m.room.MemberEvent
import net.folivo.matrix.restclient.model.events.m.room.PowerLevelsEvent
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.util.LinkedMultiValueMap
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
    fun getEvent(roomId: String, eventId: String): Mono<Event<*>> {
        return webClient
                .get().uri(
                        "/r0/rooms/{roomId}/event/{eventId}",
                        roomId,
                        eventId
                )
                .retrieve()
                .bodyToMono()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-state-eventtype-statekey">matrix spec</a>
     */
    inline fun <reified T : EventContent> getStateEvent(
            roomId: String,
            stateKey: String = "",
            eventType: String? = null
    ): Mono<T> {
        return getStateEvent(T::class.java, roomId, stateKey, eventType)
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-state-eventtype-statekey">matrix spec</a>
     */
    fun <T : EventContent> getStateEvent(
            eventContentClass: Class<T>,
            roomId: String,
            stateKey: String = "",
            eventType: String? = null
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
                .get().uri(
                        "/r0/rooms/{roomId}/state/{eventType}/{stateKey}",
                        roomId,
                        annotatedEventType,
                        stateKey
                )
                .retrieve()
                .bodyToMono(eventContentClass)
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-state">matrix spec</a>
     */
    fun getState(roomId: String): Flux<StateEvent<*, *>> {
        return webClient
                .get().uri(
                        "/r0/rooms/{roomId}/state",
                        roomId
                )
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
            notMembership: Membership? = null
    ): Flux<MemberEvent> {
        val params = LinkedMultiValueMap<String, String>();
        at?.also { params.add("at", it) }
        membership?.also { params.add("membership", it.value) }
        notMembership?.also { params.add("not_membership", it.value) }
        return webClient
                .get().uri {
                    it.path("/r0/rooms/{roomId}/members")
                            .queryParams(params)
                            .build(roomId)
                }
                .retrieve()
                .bodyToMono<GetMembersResponse>()
                .flatMapMany { Flux.fromIterable(it.chunk) }
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-rooms-roomid-joined-members">matrix spec</a>
     */
    fun getJoinedMembers(roomId: String): Mono<GetJoinedMembersResponse> {
        return webClient
                .get().uri(
                        "/r0/rooms/{roomId}/joined_members",
                        roomId
                )
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
            filter: String? = null
    ): Mono<GetEventsResponse> {
        val params = LinkedMultiValueMap<String, String>();
        from.also { params.add("from", it) }
        to?.also { params.add("to", it) }
        dir.also { params.add("dir", it.value) }
        limit.also { params.add("limit", it.toString()) }
        filter?.also { params.add("filter", it) }
        return webClient
                .get().uri {
                    it.path("/r0/rooms/{roomId}/messages")
                            .queryParams(params)
                            .build(roomId)
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
            eventType: String? = null
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
                .put().uri(
                        "/r0/rooms/{roomId}/state/{eventType}/{stateKey}",
                        roomId,
                        annotatedEventType,
                        stateKey
                )
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
            txnId: String = UUID.randomUUID().toString()
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
                .put().uri(
                        "/r0/rooms/{roomId}/send/{eventType}/{txnId}",
                        roomId,
                        annotatedEventType,
                        txnId
                )
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
            txnId: String = UUID.randomUUID().toString()
    ): Mono<String> {
        return webClient
                .put().uri(
                        "/r0/rooms/{roomId}/redact/{eventId}/{txnId}",
                        roomId,
                        eventId,
                        txnId
                )
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
            invite: List<String>? = null,
            invite3Pid: List<Invite3Pid>? = null,
            roomVersion: String? = null,
            creationContent: CreateEvent.CreateEventContent? = null,
            initialState: List<StateEvent<*, *>>? = null,
            preset: Preset? = null,
            isDirect: Boolean? = null,
            powerLevelContentOverride: PowerLevelsEvent.PowerLevelsEventContent? = null
    ): Mono<String> {
        return webClient
                .post().uri("/r0/createRoom")
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
            roomAlias: String
    ): Mono<Void> {
        return webClient
                .put().uri(
                        "/r0/directory/room/{roomAlias}",
                        roomAlias
                )
                .bodyValue(mapOf("room_id" to roomId))
                .retrieve()
                .bodyToMono<Void>()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-directory-room-roomalias">matrix spec</a>
     */
    fun getRoomAlias(
            roomAlias: String
    ): Mono<GetRoomAliasResponse> {
        return webClient
                .get().uri(
                        "/r0/directory/room/{roomAlias}",
                        roomAlias
                )
                .retrieve()
                .bodyToMono<GetRoomAliasResponse>()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#delete-matrix-client-r0-directory-room-roomalias">matrix spec</a>
     */
    fun deleteRoomAlias(
            roomAlias: String
    ): Mono<Void> {
        return webClient
                .delete().uri(
                        "/r0/directory/room/{roomAlias}",
                        roomAlias
                )
                .retrieve()
                .bodyToMono<Void>()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-joined-rooms">matrix spec</a>
     */
    fun getJoinedRooms(): Flux<String> {
        return webClient
                .get().uri("/r0/joined_rooms")
                .retrieve()
                .bodyToMono<GetJoinedRoomsResponse>()
                .flatMapMany { Flux.fromIterable(it.joinedRooms) }
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-rooms-roomid-invite">matrix spec</a>
     */
    fun inviteUser(roomId: String, userId: String): Mono<Void> {
        return webClient
                .post().uri("/r0/rooms/{roomId}/invite", roomId)
                .bodyValue(mapOf("user_id" to userId))
                .retrieve()
                .bodyToMono<Void>()
    }

    // TODO does it work with empty servers? Maybe use also https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-rooms-roomid-join
    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-join-roomidoralias">matrix spec</a>
     */
    fun joinRoom(
            roomIdOrAlias: String,
            serverNames: List<String>? = null,
            thirdPartySigned: ThirdPartySigned? = null
    ): Mono<String> {
        val params = LinkedMultiValueMap<String, String>()
        serverNames?.also { params.addAll("server_name", it) }
        return webClient
                .post().uri {
                    it.path("/r0/join/{roomIdOrAlias}")
                            .queryParams(params)
                            .build(roomIdOrAlias)
                }
                .bodyValue(mapOf("third_party_signed" to thirdPartySigned))
                .retrieve()
                .bodyToMono<JoinRoomResponse>()
                .map { it.roomId }
    }

//    /**
//     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-rooms-roomid-leave">matrix spec</a>
//     */
//    fun leaveRoom() {
//        // TODO implement
//    }

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