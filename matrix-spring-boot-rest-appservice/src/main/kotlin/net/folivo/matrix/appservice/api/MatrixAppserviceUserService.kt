package net.folivo.matrix.appservice.api

interface MatrixAppserviceUserService {

    enum class UserExistingState {
        EXISTS, DOES_NOT_EXISTS, CAN_BE_CREATED
    }

    fun userExistingState(userId: String): UserExistingState
    fun saveUser(userId: String)
}