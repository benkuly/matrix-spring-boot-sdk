package net.folivo.matrix.bot.appservice.user

import kotlinx.coroutines.flow.Flow
import net.folivo.matrix.core.model.MatrixUserId
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixUserRepository : CoroutineCrudRepository<MatrixUser, MatrixUserId> {

    @Query(
            """
        SELECT * FROM matrix_user u
        JOIN matrix_membership m ON m.user_id = u.id
        WHERE m.room_id = :roomId
        """
    )
    fun findByRoomId(roomId: String): Flow<MatrixUser>
}