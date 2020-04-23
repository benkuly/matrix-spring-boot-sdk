package net.folivo.matrix.restclient.model.events

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

data class StrippedStateEvent(
        @JsonProperty("type")
        val type: String,
        @JsonProperty("content")
        val content: JsonNode, // TODO should be event content, so we should handle StrippedStateEvent as subset of StateEvent
        @JsonProperty("state_key")
        val stateKey: String,
        @JsonProperty("sender")
        val sender: String
)