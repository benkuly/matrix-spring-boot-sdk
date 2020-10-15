package net.folivo.matrix.bot.appservice.room

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixRoomAliasRepository : CoroutineCrudRepository<MatrixRoomAlias, String> {

}