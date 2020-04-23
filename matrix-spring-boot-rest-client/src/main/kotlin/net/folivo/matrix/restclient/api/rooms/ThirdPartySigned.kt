package net.folivo.matrix.restclient.api.rooms

import com.fasterxml.jackson.annotation.JsonProperty

data class ThirdPartySigned(
        @JsonProperty("sender")
        val sender: String,
        @JsonProperty("mxid")
        val mxid: String,
        @JsonProperty("token")
        val token: String,
        @JsonProperty("signatures")
        val signatures: Map<String, Map<String, String>>
)