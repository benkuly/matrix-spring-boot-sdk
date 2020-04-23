package net.folivo.matrix.restclient.api.rooms

import com.fasterxml.jackson.annotation.JsonProperty

data class Invite3Pid(
        @JsonProperty("id_server")
        val identityServer: String,
        @JsonProperty("id_access_token")
        val identityServerAccessToken: String,
        @JsonProperty("medium")
        val medium: String,
        @JsonProperty("address")
        val address: String
)