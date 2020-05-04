package net.folivo.matrix.appservice.api.user

interface MatrixAppserviceUserService {

    enum class UserExistingState {
        EXISTS, DOES_NOT_EXISTS, CAN_BE_CREATED
    }

    fun userExistingState(userId: String): UserExistingState
    fun getCreateUserParameter(userId: String): CreateUserParameter
    fun saveUser(userId: String)
}