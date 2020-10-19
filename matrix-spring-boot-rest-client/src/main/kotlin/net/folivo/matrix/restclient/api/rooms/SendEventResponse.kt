package net.folivo.matrix.restclient.api.rooms

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.model.MatrixId.EventId

internal data class SendEventResponse(
        @JsonProperty("event_id")
        val eventId: EventId
)