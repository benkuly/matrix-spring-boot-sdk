package net.folivo.matrix.bot.appservice.room

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatrixRoomRepository : CoroutineCrudRepository<MatrixRoom, String> { //FIXME test queries

    @Query(
            """
            WITH countedRooms AS (SELECT COUNT(*) AS memberSize,m.fk_Membership_MatrixRoom FROM Membership m 
            WHERE m.fk_Membership_MatrixUser IN :members 
            GROUP_BY m.fk_Membership_MatrixRoom) 
            SELECT * FROM MatrixRoom room 
            JOIN countedRooms ON countedRooms.fk_Membership_MatrixRoom = room.id 
            WHERE countedRooms.memberSize = :#{#members.size}
            """
    )
    fun findByContainingMembers(members: Set<String>): Flow<MatrixRoom>

    @Query(
            """
            SELECT * FROM MatrixRoom r 
            JOIN Membership m ON m.k_Membership_MatrixRoom = r.id 
            WHERE m.fk_Membership_MatrixUser = :userId AND m.mappingToken = :mappingToken
            """
    )
    suspend fun findByMemberAndMappingToken(userId: String, mappingToken: Int): MatrixRoom?

    @Query(
            """
            SELECT * FROM MatrixRoom r 
            JOIN Membership m ON m.k_Membership_MatrixRoom = r.id 
            WHERE m.fk_Membership_MatrixUser = :userId
            """
    )
    fun findByMember(userId: String): Flow<MatrixRoom>
}