package net.folivo.matrix.restclient.api.server

import com.fasterxml.jackson.annotation.JsonProperty

data class VersionsResponse(
        @JsonProperty("versions")
        val versions: List<String>,
        @JsonProperty("unstable_features")
        val unstable_features: Map<String, Boolean>
)