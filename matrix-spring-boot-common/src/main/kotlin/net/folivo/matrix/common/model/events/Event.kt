package net.folivo.matrix.restclient.model.events

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo


/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#event-fields">matrix spec</a>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        defaultImpl = UnknownEvent::class,
        visible = true
)
abstract class Event<C : EventContent>(
        @JsonProperty("type")
        val type: String,
        @JsonProperty("content")
        val content: C
)