package net.folivo.matrix.appservice.api.user

class NoOpMatrixAppserviceUserService : MatrixAppserviceUserService {
    override fun userExistingState(username: String): MatrixAppserviceUserService.UserExistingState {
        return MatrixAppserviceUserService.UserExistingState.DOES_NOT_EXISTS
    }

    override fun getCreateUserParameter(username: String): CreateUserParameter {
        return CreateUserParameter()
    }

    override fun saveUser(username: String) {
    }
}