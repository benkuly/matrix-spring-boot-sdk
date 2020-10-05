package net.folivo.matrix.appservice.api.user

interface AppserviceUserService {

    enum class UserExistingState {
        EXISTS, DOES_NOT_EXISTS, CAN_BE_CREATED
    }

    suspend fun userExistingState(userId: String): UserExistingState
    suspend fun getRegisterUserParameter(userId: String): RegisterUserParameter
    suspend fun onRegisteredUser(userId: String)
}