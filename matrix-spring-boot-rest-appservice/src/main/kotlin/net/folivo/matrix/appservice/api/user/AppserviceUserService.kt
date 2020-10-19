package net.folivo.matrix.appservice.api.user

import net.folivo.matrix.core.model.MatrixId.UserId

interface AppserviceUserService {

    enum class UserExistingState {
        EXISTS, DOES_NOT_EXISTS, CAN_BE_CREATED
    }

    suspend fun userExistingState(userId: UserId): UserExistingState
    suspend fun getRegisterUserParameter(userId: UserId): RegisterUserParameter
    suspend fun onRegisteredUser(userId: UserId)
}