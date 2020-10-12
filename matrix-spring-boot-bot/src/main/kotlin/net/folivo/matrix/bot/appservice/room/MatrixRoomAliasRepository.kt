package net.folivo.matrix.bot.appservice.room

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixRoomAliasRepository : ReactiveCrudRepository<MatrixRoomAlias, String> {

}