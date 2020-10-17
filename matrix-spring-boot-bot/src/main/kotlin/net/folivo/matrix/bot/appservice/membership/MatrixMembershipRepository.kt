package net.folivo.matrix.bot.appservice.membership

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixMembershipRepository : CoroutineCrudRepository<MatrixMembership, String> {

    fun findByRoomId(roomId: String): Flow<MatrixMembership>

    fun findByUserId(userId: String): Flow<MatrixMembership>

    suspend fun countByRoomId(roomId: String): Long

    suspend fun countByUserId(userId: String): Long

    suspend fun findByUserIdAndRoomId(userId: String, roomId: String): MatrixMembership?

    suspend fun deleteByUserIdAndRoomId(userId: String, roomId: String)

    @Query(
            """
        SELECT CASE WHEN COUNT(*) = :#{#members.size()} THEN true ELSE false END
        FROM matrix_membership m
        WHERE m.room_id = :roomId AND m.user_id IN (:members)
        """
    )
    suspend fun containsMembersByRoomId(roomId: String, members: Set<String>): Boolean

    @Query(
            """
            SELECT CASE WHEN COUNT(*) = 0 THEN true ELSE false END
            FROM matrix_membership m
            JOIN matrix_user u ON m.user_id = u.id
            WHERE m.room_id = :roomId AND u.is_managed = false
            """
    )
    suspend fun containsOnlyManagedMembersByRoomId(roomId: String): Boolean

}