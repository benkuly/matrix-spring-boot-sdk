package net.folivo.matrix.restclient.api.rooms

import com.fasterxml.jackson.annotation.JsonProperty

internal data class SendEventResponse(
        @JsonProperty("event_id")
        val eventId: String
)