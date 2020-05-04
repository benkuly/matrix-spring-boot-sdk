package net.folivo.matrix.bot.appservice.room

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AppserviceRoomRepository : CrudRepository<AppserviceRoom, Long> {
    fun findByMatrixRoomAlias(matrixRoomAlias: String): AppserviceRoom?
}