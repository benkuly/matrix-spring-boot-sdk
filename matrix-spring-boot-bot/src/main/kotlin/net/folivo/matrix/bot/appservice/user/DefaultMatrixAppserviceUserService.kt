package net.folivo.matrix.bot.appservice.user

import net.folivo.matrix.appservice.api.user.CreateUserParameter
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.bot.appservice.AppserviceBotManager

// FIXME test
class DefaultMatrixAppserviceUserService(
        private val appserviceBotManager: AppserviceBotManager,
        private val appserviceUserRepository: AppserviceUserRepository
) : MatrixAppserviceUserService {

    override fun userExistingState(userId: String): MatrixAppserviceUserService.UserExistingState {
        val matrixUsername = userId.trimStart('@').substringBefore(":")
        return if (appserviceUserRepository.findByMatrixUsername(matrixUsername) != null) {
            MatrixAppserviceUserService.UserExistingState.EXISTS
        } else if (appserviceBotManager.shouldCreateUser(matrixUsername)) {
            MatrixAppserviceUserService.UserExistingState.CAN_BE_CREATED
        } else {
            MatrixAppserviceUserService.UserExistingState.DOES_NOT_EXISTS
        }
    }

    override fun getCreateUserParameter(userId: String): CreateUserParameter {
        TODO("Not yet implemented")
    }

    override fun saveUser(userId: String) {
        val matrixUsername = userId.trimStart('@').substringBefore(":")
        appserviceUserRepository.save(AppserviceUser(matrixUsername))
    }
}