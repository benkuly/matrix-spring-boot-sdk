package net.folivo.matrix.appservice.api.event

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.model.events.Event

data class EventRequest(
        @JsonProperty("events")
        val events: List<Event<*>>
)