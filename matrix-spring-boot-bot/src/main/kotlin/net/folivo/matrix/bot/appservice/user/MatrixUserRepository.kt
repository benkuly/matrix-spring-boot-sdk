package net.folivo.matrix.bot.appservice.user

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface MatrixUserRepository : ReactiveCrudRepository<MatrixUser, String> {

    @Query(
            """
            SELECT * FROM MatrixUser a 
            JOIN Membership m ON m.fk_Membership_MatrixUser = a.id 
            WHERE m.fk_Membership_MatrixRoom = :roomId
            """
    )
    fun findByRoomId(roomId: String): Flux<MatrixUser>
}