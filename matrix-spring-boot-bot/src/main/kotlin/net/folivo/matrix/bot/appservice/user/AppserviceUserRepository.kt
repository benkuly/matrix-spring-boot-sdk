package net.folivo.matrix.bot.appservice.user

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AppserviceUserRepository : CrudRepository<AppserviceUser, Long> {
    fun findByMatrixUsername(matrixUsername: String): AppserviceUser?
}