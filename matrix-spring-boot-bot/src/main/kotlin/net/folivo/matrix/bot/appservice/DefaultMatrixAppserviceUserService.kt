package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.user.CreateUserParameter
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService.UserExistingState
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService.UserExistingState.CAN_BE_CREATED
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService.UserExistingState.EXISTS
import reactor.core.publisher.Mono

open class DefaultMatrixAppserviceUserService(
        private val helper: MatrixAppserviceServiceHelper
) : MatrixAppserviceUserService {
    override fun userExistingState(userId: String): Mono<UserExistingState> {
        return helper.shouldCreateUser(userId)
                .map { if (it) CAN_BE_CREATED else EXISTS }
    }

    override fun getCreateUserParameter(userId: String): Mono<CreateUserParameter> {
        return Mono.just(CreateUserParameter())
    }

    override fun saveUser(userId: String): Mono<Void> {
        return Mono.empty()
    }
}