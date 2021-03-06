package net.folivo.matrix.restclient.api.rooms

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.model.events.RoomEvent
import net.folivo.matrix.core.model.events.StateEvent

data class GetEventsResponse(
        @JsonProperty("start")
        val start: String,
        @JsonProperty("end")
        val end: String,
        @JsonProperty("chunk")
        val chunk: List<RoomEvent<*, *>>,
        @JsonProperty("state")
        val state: List<StateEvent<*, *>>
)