package net.folivo.spring.matrix.bot.membership

import kotlinx.coroutines.flow.Flow
import net.folivo.trixnity.core.model.MatrixId
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixMembershipRepository : CoroutineCrudRepository<MatrixMembership, String> {

    fun findByRoomId(roomId: MatrixId.RoomId): Flow<MatrixMembership>

    fun findByUserId(userId: MatrixId.UserId): Flow<MatrixMembership>

    suspend fun countByRoomId(roomId: MatrixId.RoomId): Long

    suspend fun countByUserId(userId: MatrixId.UserId): Long

    suspend fun findByUserIdAndRoomId(userId: MatrixId.UserId, roomId: MatrixId.RoomId): MatrixMembership?

    suspend fun deleteByUserIdAndRoomId(userId: MatrixId.UserId, roomId: MatrixId.RoomId)

    @Query(
        """
        SELECT CASE WHEN COUNT(*) = :#{#members.size()} THEN true ELSE false END
        FROM matrix_membership m
        WHERE m.room_id = :roomId AND m.user_id IN (:members)
        """
    )
    suspend fun containsMembersByRoomId(roomId: MatrixId.RoomId, members: Set<MatrixId.UserId>): Boolean

    @Query(
        """
            SELECT CASE WHEN COUNT(*) = 0 THEN true ELSE false END
            FROM matrix_membership m
            JOIN matrix_user u ON m.user_id = u.id
            WHERE m.room_id = :roomId AND u.is_managed = false
            """
    )
    suspend fun containsOnlyManagedMembersByRoomId(roomId: MatrixId.RoomId): Boolean

}