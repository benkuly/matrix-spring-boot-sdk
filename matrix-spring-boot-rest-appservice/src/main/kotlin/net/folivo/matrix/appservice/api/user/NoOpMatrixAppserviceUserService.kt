package net.folivo.matrix.appservice.api.user

class NoOpMatrixAppserviceUserService : MatrixAppserviceUserService {
    override fun userExistingState(userId: String): MatrixAppserviceUserService.UserExistingState {
        return MatrixAppserviceUserService.UserExistingState.DOES_NOT_EXISTS
    }

    override fun getCreateUserParameter(userId: String): CreateUserParameter {
        return CreateUserParameter()
    }

    override fun saveUser(userId: String) {
    }
}