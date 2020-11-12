package net.folivo.matrix.bot.room

import net.folivo.matrix.core.model.MatrixId.RoomAliasId
import net.folivo.matrix.core.model.MatrixId.RoomId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixRoomAliasRepository : CoroutineCrudRepository<MatrixRoomAlias, RoomAliasId> {

    suspend fun findByRoomId(roomId: RoomId): MatrixRoomAlias?//FIXME test

}