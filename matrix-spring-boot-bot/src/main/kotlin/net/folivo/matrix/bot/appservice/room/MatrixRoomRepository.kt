package net.folivo.matrix.bot.appservice.room

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface MatrixRoomRepository : ReactiveCrudRepository<MatrixRoom, String> { //FIXME test queries

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
    fun findByContainingMembers(members: Set<String>): Flux<MatrixRoom>

    @Query(
            """
            SELECT * FROM MatrixRoom r 
            JOIN Membership m ON m.k_Membership_MatrixRoom = r.id 
            WHERE m.fk_Membership_MatrixUser = :userId AND m.mappingToken = :mappingToken
            """
    )
    fun findByMemberAndMappingToken(userId: String, mappingToken: Int): Mono<MatrixRoom>

    @Query(
            """
            SELECT * FROM MatrixRoom r 
            JOIN Membership m ON m.k_Membership_MatrixRoom = r.id 
            WHERE m.fk_Membership_MatrixUser = :userId
            """
    )
    fun findByMember(userId: String): Flux<MatrixRoom>
}