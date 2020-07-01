package net.folivo.matrix.appservice.api.user

interface MatrixAppserviceUserService {

    enum class UserExistingState {
        EXISTS, DOES_NOT_EXISTS, CAN_BE_CREATED
    }

    suspend fun userExistingState(userId: String): UserExistingState
    suspend fun getCreateUserParameter(userId: String): CreateUserParameter
    suspend fun saveUser(userId: String)
}