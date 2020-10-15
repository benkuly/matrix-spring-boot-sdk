package net.folivo.matrix.bot.appservice.user

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixUserRepository : CoroutineCrudRepository<MatrixUser, String> {

    @Query(
            """
            SELECT * FROM MatrixUser a 
            JOIN Membership m ON m.fk_Membership_MatrixUser = a.id 
            WHERE m.fk_Membership_MatrixRoom = :roomId
            """
    )
    fun findByRoomId(roomId: String): Flow<MatrixUser>
}