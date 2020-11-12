package net.folivo.matrix.restclient.api.user

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.model.MatrixId.UserId

data class RegisterResponse(
        @JsonProperty("user_id")
        val userId: UserId,
        @JsonProperty("access_token")
        val accessToken: String? = null,
        @JsonProperty("device_id")
        val deviceId: String? = null
)