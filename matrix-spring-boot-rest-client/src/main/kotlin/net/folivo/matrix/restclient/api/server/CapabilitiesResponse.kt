package net.folivo.matrix.restclient.api.server

import com.fasterxml.jackson.annotation.JsonProperty

data class CapabilitiesResponse(
        @JsonProperty("capabilities")
        val capabilities: Capabilities
) {
    data class Capabilities(
            @JsonProperty("m.change_password")
            val changePassword: ChangePasswordCapability,
            @JsonProperty("m.room_versions")
            val roomVersion: RoomVersionsCapability
    )

    data class ChangePasswordCapability(
            @JsonProperty("enabled")
            val enabled: Boolean
    )

    data class RoomVersionsCapability(
            @JsonProperty("default")
            val default: String,
            @JsonProperty("available")
            val available: Map<String, RoomVersionStability>
    ) {
        enum class RoomVersionStability {
            @JsonProperty("stable")
            STABLE,

            @JsonProperty("unstable")
            UNSTABLE
        }
    }
}