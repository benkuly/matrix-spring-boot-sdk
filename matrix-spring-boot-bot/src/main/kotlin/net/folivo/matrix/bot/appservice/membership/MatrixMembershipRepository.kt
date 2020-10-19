package net.folivo.matrix.bot.appservice.membership

import kotlinx.coroutines.flow.Flow
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixMembershipRepository : CoroutineCrudRepository<MatrixMembership, String> {

    fun findByRoomId(roomId: RoomId): Flow<MatrixMembership>

    fun findByUserId(userId: UserId): Flow<MatrixMembership>

    suspend fun countByRoomId(roomId: RoomId): Long

    suspend fun countByUserId(userId: UserId): Long

    suspend fun findByUserIdAndRoomId(userId: UserId, roomId: RoomId): MatrixMembership?

    suspend fun deleteByUserIdAndRoomId(userId: UserId, roomId: RoomId)

    @Query(
            """
        SELECT CASE WHEN COUNT(*) = :#{#members.size()} THEN true ELSE false END
        FROM matrix_membership m
        WHERE m.room_id = :roomId AND m.user_id IN (:members)
        """
    )
    suspend fun containsMembersByRoomId(roomId: RoomId, members: Set<UserId>): Boolean

    @Query(
            """
            SELECT CASE WHEN COUNT(*) = 0 THEN true ELSE false END
            FROM matrix_membership m
            JOIN matrix_user u ON m.user_id = u.id
            WHERE m.room_id = :roomId AND u.is_managed = false
            """
    )
    suspend fun containsOnlyManagedMembersByRoomId(roomId: RoomId): Boolean

}