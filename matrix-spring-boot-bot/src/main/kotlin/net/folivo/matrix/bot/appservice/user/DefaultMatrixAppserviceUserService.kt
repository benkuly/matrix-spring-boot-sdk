package net.folivo.matrix.bot.appservice.user

import net.folivo.matrix.appservice.api.user.CreateUserParameter
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.bot.appservice.AppserviceBotManager

// FIXME test
class DefaultMatrixAppserviceUserService(
        private val appserviceBotManager: AppserviceBotManager,
        private val appserviceUserRepository: AppserviceUserRepository
) : MatrixAppserviceUserService {

    override fun userExistingState(username: String): MatrixAppserviceUserService.UserExistingState {
        return if (appserviceUserRepository.findByMatrixUsername(username) != null) {
            MatrixAppserviceUserService.UserExistingState.EXISTS
        } else if (appserviceBotManager.shouldCreateUser(username)) {
            MatrixAppserviceUserService.UserExistingState.CAN_BE_CREATED
        } else {
            MatrixAppserviceUserService.UserExistingState.DOES_NOT_EXISTS
        }
    }

    override fun getCreateUserParameter(username: String): CreateUserParameter {
        return appserviceBotManager.getCreateUserParameter(username)
    }

    override fun saveUser(username: String) {
        appserviceUserRepository.save(AppserviceUser(username))
    }
}