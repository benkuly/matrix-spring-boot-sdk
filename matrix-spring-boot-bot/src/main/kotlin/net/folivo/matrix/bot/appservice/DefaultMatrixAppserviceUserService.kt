package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.user.CreateUserParameter
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService.UserExistingState
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService.UserExistingState.CAN_BE_CREATED
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService.UserExistingState.EXISTS

open class DefaultMatrixAppserviceUserService(
        private val helper: MatrixAppserviceServiceHelper
) : MatrixAppserviceUserService {
    override suspend fun userExistingState(userId: String): UserExistingState {
        return if (helper.isManagedUser(userId)) CAN_BE_CREATED else EXISTS
    }

    override suspend fun getCreateUserParameter(userId: String): CreateUserParameter {
        return CreateUserParameter()
    }

    override suspend fun saveUser(userId: String) {
    }
}