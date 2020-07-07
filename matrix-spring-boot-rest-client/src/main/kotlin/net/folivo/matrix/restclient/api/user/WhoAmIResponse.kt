package net.folivo.matrix.restclient.api.user

import com.fasterxml.jackson.annotation.JsonProperty

data class WhoAmIResponse(
        @JsonProperty("user_id")
        val userId: String
)