package net.folivo.matrix.bot.appservice.room

import kotlinx.coroutines.flow.Flow
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixRoomRepository : CoroutineCrudRepository<MatrixRoom, RoomId> {

    @Query(
            """
        WITH counted_rooms AS
            (SELECT room_id FROM matrix_membership m
            WHERE m.user_id IN (:members)
            GROUP BY m.room_id
            HAVING COUNT(m.user_id) = :#{#members.size()})
        SELECT * FROM matrix_room r
        JOIN counted_rooms ON counted_rooms.room_id = r.id
        """
    )
    fun findByMembers(members: Set<UserId>): Flow<MatrixRoom>
}