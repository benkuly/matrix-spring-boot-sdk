package net.folivo.matrix.restclient.api.user

import net.folivo.matrix.core.model.MatrixId.UserId
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class UserApiClient(private val webClient: WebClient) {

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#post-matrix-client-r0-register">matrix spec</a>
     */
    suspend fun register(
            authenticationType: String,
            authenticationSession: String? = null,
            username: String? = null,
            password: String? = null,
            accountType: AccountType? = null,
            deviceId: String? = null,
            initialDeviceDisplayName: String? = null,
            inhibitLogin: Boolean? = null
    ): RegisterResponse {
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
                .awaitBody()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#put-matrix-client-r0-profile-userid-displayname">matrix spec</a>
     */
    suspend fun setDisplayName(
            userId: UserId,
            displayName: String? = null,
            asUserId: UserId? = null
    ) {
        return webClient
                .put().uri {
                    it.apply {
                        path("/r0/profile/{userId}/displayname")
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build(userId.full)
                }
                .bodyValue(mapOf("displayname" to displayName))
                .retrieve()
                .awaitBody()
    }

    /**
     * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#get-matrix-client-r0-account-whoami">matrix spec</a>
     */
    suspend fun whoAmI(asUserId: UserId? = null): UserId {
        return webClient
                .get().uri {
                    it.apply {
                        path("/r0/account/whoami")
                        if (asUserId != null) queryParam("user_id", asUserId.full)
                    }.build()
                }
                .retrieve()
                .awaitBody<WhoAmIResponse>()
                .userId
    }

}