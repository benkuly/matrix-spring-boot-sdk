package net.folivo.matrix.restclient.api.user

import com.fasterxml.jackson.annotation.JsonProperty

data class RegisterResponse(
        @JsonProperty("user_id")
        val userId: String,
        @JsonProperty("access_token")
        val accessToken: String? = null,
        @JsonProperty("device_id")
        val deviceId: String? = null
)