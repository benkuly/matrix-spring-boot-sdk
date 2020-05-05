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
        return if (appserviceUserRepository.findById(userId).isPresent) {
            MatrixAppserviceUserService.UserExistingState.EXISTS
        } else if (appserviceBotManager.shouldCreateUser(userId)) {
            MatrixAppserviceUserService.UserExistingState.CAN_BE_CREATED
        } else {
            MatrixAppserviceUserService.UserExistingState.DOES_NOT_EXISTS
        }
    }

    override fun getCreateUserParameter(userId: String): CreateUserParameter {
        return appserviceBotManager.getCreateUserParameter(userId)
    }

    override fun saveUser(userId: String) {
        appserviceUserRepository.save(AppserviceUser(userId))
    }
}