package net.folivo.matrix.appservice.api.user

import reactor.core.publisher.Mono

interface MatrixAppserviceUserService {

    enum class UserExistingState {
        EXISTS, DOES_NOT_EXISTS, CAN_BE_CREATED
    }

    fun userExistingState(userId: String): Mono<UserExistingState>
    fun getCreateUserParameter(userId: String): Mono<CreateUserParameter>
    fun saveUser(userId: String): Mono<Void>
}