package net.folivo.matrix.restclient.api.user

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.model.MatrixId.UserId

data class WhoAmIResponse(
        @JsonProperty("user_id")
        val userId: UserId
)