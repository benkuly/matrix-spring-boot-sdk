package net.folivo.matrix.common.model.events

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#room-event-fields">matrix spec</a>
 */
abstract class RoomEvent<C : RoomEventContent, U : RoomEvent.UnsignedData> : Event<C> {

    constructor(
            type: String,
            content: C,
            id: String,
            sender: String,
            originTimestamp: Long,
            roomId: String? = null,
            unsigned: U? = null
    ) : super(
            type = type,
            content = content
    ) {
        this.id = id
        this.sender = sender
        this.originTimestamp = originTimestamp
        this.roomId = roomId
        this.unsigned = unsigned
    }

    @JsonProperty("event_id")
    val id: String

    @JsonProperty("sender")
    val sender: String

    @JsonProperty("origin_server_ts")
    val originTimestamp: Long

    @JsonProperty("room_id")
    val roomId: String?

    @JsonProperty("unsigned")
    val unsigned: U?

    open class UnsignedData(
            @JsonProperty("age")
            val age: Long? = null,

            @JsonProperty("redactedBecause")
            val redactedBecause: Event<*>? = null,

            @JsonProperty("transaction_id")
            val transactionId: String? = null
    )

}