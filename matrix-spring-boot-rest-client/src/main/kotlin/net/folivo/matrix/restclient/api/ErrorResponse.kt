package net.folivo.matrix.restclient.api

import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorResponse(
        @JsonProperty("errcode")
        val errorCode: String,
        @JsonProperty("error")
        val errorMessage: String
)