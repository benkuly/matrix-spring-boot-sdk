package net.folivo.matrix.bot.appservice.room

import net.folivo.matrix.core.model.MatrixId.RoomAliasId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixRoomAliasRepository : CoroutineCrudRepository<MatrixRoomAlias, RoomAliasId> {

}