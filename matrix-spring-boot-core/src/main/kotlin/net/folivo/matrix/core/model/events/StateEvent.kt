package net.folivo.matrix.core.model.events

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.model.MatrixId.*


/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#state-event-fields">matrix spec</a>
 */
abstract class StateEvent<C : StateEventContent, U : StateEvent.UnsignedData> : Event<C> {

    constructor(
            type: String,
            content: C,
            id: EventId,
            sender: UserId,
            originTimestamp: Long,
            roomId: RoomId? = null,
            unsigned: U,
            stateKey: String,
            previousContent: C? = null
    ) : super(
            type = type,
            content = content
    ) {
        this.stateKey = stateKey
        this.previousContent = previousContent
        this.id = id
        this.sender = sender
        this.originTimestamp = originTimestamp
        this.roomId = roomId
        this.unsigned = unsigned
    }

    @JsonProperty("event_id")
    val id: EventId

    @JsonProperty("sender")
    val sender: UserId

    @JsonProperty("origin_server_ts")
    val originTimestamp: Long

    @JsonProperty("room_id")
    val roomId: RoomId?

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

    @JsonProperty("state_key")
    val stateKey: String

    @JsonProperty("prev_content")
    val previousContent: C?
}