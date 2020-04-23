package net.folivo.matrix.restclient.api.rooms

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.restclient.model.events.m.room.MemberEvent

internal data class GetMembersResponse(
        @JsonProperty("chunk")
        val chunk: List<MemberEvent>
)