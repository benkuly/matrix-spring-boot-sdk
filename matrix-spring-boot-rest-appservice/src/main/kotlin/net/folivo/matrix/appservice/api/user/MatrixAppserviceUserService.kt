package net.folivo.matrix.appservice.api.user

interface MatrixAppserviceUserService {

    enum class UserExistingState {
        EXISTS, DOES_NOT_EXISTS, CAN_BE_CREATED
    }

    fun userExistingState(username: String): UserExistingState
    fun getCreateUserParameter(username: String): CreateUserParameter
    fun saveUser(username: String)
}