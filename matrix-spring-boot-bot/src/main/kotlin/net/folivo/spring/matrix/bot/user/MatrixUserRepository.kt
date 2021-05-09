package net.folivo.spring.matrix.bot.user

import kotlinx.coroutines.flow.Flow
import net.folivo.trixnity.core.model.MatrixId
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixUserRepository : CoroutineCrudRepository<MatrixUser, MatrixId.UserId> {

    @Query(
        """
        SELECT * FROM matrix_user u
        JOIN matrix_membership m ON m.user_id = u.id
        WHERE m.room_id = :roomId
        """
    )
    fun findByRoomId(roomId: MatrixId.RoomId): Flow<MatrixUser>
}