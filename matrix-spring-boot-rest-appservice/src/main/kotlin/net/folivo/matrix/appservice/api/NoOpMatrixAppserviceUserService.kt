package net.folivo.matrix.appservice.api

class NoOpMatrixAppserviceUserService : MatrixAppserviceUserService {
    override fun userExistingState(userId: String): MatrixAppserviceUserService.UserExistingState {
        return MatrixAppserviceUserService.UserExistingState.DOES_NOT_EXISTS
    }

    override fun saveUser(userId: String) {
    }
}