package net.folivo.matrix.restclient.api.rooms

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.model.MatrixId.UserId

data class ThirdPartySigned(
        @JsonProperty("sender")
        val sender: UserId,
        @JsonProperty("mxid")
        val mxid: UserId,
        @JsonProperty("token")
        val token: String,
        @JsonProperty("signatures")
        val signatures: Map<String, Map<String, String>>
)