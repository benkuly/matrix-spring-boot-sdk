package net.folivo.matrix.restclient.model.events

import com.fasterxml.jackson.annotation.JsonProperty


/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#state-event-fields">matrix spec</a>
 */
abstract class StateEvent<C : StateEventContent, U : StateEvent.UnsignedData> : Event<C> {

    constructor(
            type: String,
            content: C,
            id: String,
            sender: String,
            originTimestamp: Long,
            roomId: String? = null,
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

    @JsonProperty("state_key")
    val stateKey: String

    @JsonProperty("prev_content")
    val previousContent: C?
}