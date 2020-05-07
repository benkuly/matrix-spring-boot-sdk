package net.folivo.matrix.bot.appservice.user

import net.folivo.matrix.appservice.api.user.CreateUserParameter
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.bot.appservice.AppserviceBotManager
import reactor.core.publisher.Mono

// FIXME test
class DefaultMatrixAppserviceUserService(
        private val appserviceBotManager: AppserviceBotManager,
        private val appserviceUserRepository: AppserviceUserRepository
) : MatrixAppserviceUserService {

    override fun userExistingState(userId: String): Mono<MatrixAppserviceUserService.UserExistingState> {
        return appserviceUserRepository.existsById(userId)
                .flatMap { isInDatabase ->
                    if (isInDatabase) {
                        Mono.just(MatrixAppserviceUserService.UserExistingState.EXISTS)
                    } else {
                        appserviceBotManager.shouldCreateUser(userId)
                                .map { shouldCreateUser ->
                                    if (shouldCreateUser) {
                                        MatrixAppserviceUserService.UserExistingState.CAN_BE_CREATED
                                    } else {
                                        MatrixAppserviceUserService.UserExistingState.DOES_NOT_EXISTS
                                    }
                                }
                    }
                }
    }

    override fun getCreateUserParameter(userId: String): Mono<CreateUserParameter> {
        return appserviceBotManager.getCreateUserParameter(userId)
    }

    override fun saveUser(userId: String): Mono<Void> {
        return appserviceUserRepository.save(AppserviceUser(userId))
                .then()
    }
}