package net.folivo.matrix.restclient.api.user

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

class UserApiClient(private val webClient: WebClient) {

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-register">matrix spec</a>
     */
    fun register(
            authenticationType: String,
            authenticationSession: String? = null,
            username: String? = null,
            password: String? = null,
            accountType: AccountType? = null,
            deviceId: String? = null,
            initialDeviceDisplayName: String? = null,
            inhibitLogin: Boolean? = null
    ): Mono<RegisterResponse> {
        return webClient
                .post().uri {
                    it.apply {
                        path("/r0/register")
                        if (accountType != null) queryParam("kind", accountType.value)
                    }.build()
                }
                .bodyValue(
                        mapOf(
                                "auth" to mapOf(
                                        "type" to authenticationType,
                                        "session" to authenticationSession
                                ),
                                "username" to username,
                                "password" to password,
                                "device_id" to deviceId,
                                "initial_device_display_name" to initialDeviceDisplayName,
                                "inhibit_login" to inhibitLogin
                        )
                )
                .retrieve()
                .bodyToMono()
    }

    fun setDisplayName(
            userId: String,
            displayName: String? = null,
            asUserId: String? = null
    ): Mono<Void> {
        return webClient
                .put().uri {
                    it.apply {
                        path("/r0/profile/{userId}/displayname")
                        if (asUserId != null) queryParam("user_id", asUserId)
                    }.build(userId)
                }
                .bodyValue(mapOf("displayname" to displayName))
                .retrieve()
                .bodyToMono()
    }

}