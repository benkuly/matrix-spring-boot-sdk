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
            SELECT CASE WHEN COUNT(*) = :#{#members.size} THEN 'true' ELSE 'false' END 
            FROM Membership m 
            WHERE m.fk_Membership_AppserviceRoom = :roomId AND m.fk_Membership_AppserviceUser IN :members
            """
    )
    suspend fun containsMembersByRoomId(roomId: String, members: Set<String>): Boolean

    @Query(
            """
            SELECT CASE WHEN COUNT(*) = 0 THEN 'true' ELSE 'false' END 
            FROM Membership m 
            JOIN AppserviceUser u ON m.fk_Membership_AppserviceUser = u.id 
            WHERE m.fk_Membership_AppserviceRoom = :roomId AND u.isManaged = false
            """
    )
    suspend fun containsOnlyManagedMembersByRoomId(roomId: String): Boolean

}