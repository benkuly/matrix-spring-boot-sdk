package net.folivo.spring.matrix.bot.room

import net.folivo.trixnity.core.model.MatrixId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixRoomAliasRepository : CoroutineCrudRepository<MatrixRoomAlias, MatrixId.RoomAliasId> {

    suspend fun findByRoomId(roomId: MatrixId.RoomId): MatrixRoomAlias?

}