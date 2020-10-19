package net.folivo.matrix.bot

import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.folivo.matrix.bot.membership.MatrixMembership
import net.folivo.matrix.bot.room.MatrixRoom
import net.folivo.matrix.bot.user.MatrixUser
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.from
import org.springframework.data.r2dbc.core.into
import org.springframework.data.relational.core.query.CriteriaDefinition

class RepositoryTestHelper(private val databaseClient: DatabaseClient) {

    suspend fun insertMembership(membership: MatrixMembership) {
        databaseClient.insert()
                .into<MatrixMembership>()
                .using(membership)
                .then().awaitFirstOrNull()
    }

    suspend fun deleteAllMemberships() {
        databaseClient.delete()
                .from<MatrixMembership>()
                .matching(CriteriaDefinition.empty())
                .then().awaitFirstOrNull()
    }

    suspend fun insertUser(user: MatrixUser) {
        databaseClient.insert()
                .into<MatrixUser>()
                .using(user)
                .then().awaitFirstOrNull()
    }

    suspend fun deleteAllUsers() {
        databaseClient.delete()
                .from<MatrixUser>()
                .matching(CriteriaDefinition.empty())
                .then().awaitFirstOrNull()
    }

    suspend fun insertRoom(room: MatrixRoom) {
        databaseClient.insert()
                .into<MatrixRoom>()
                .using(room)
                .then().awaitFirstOrNull()
    }

    suspend fun deleteAllRooms() {
        databaseClient.delete()
                .from<MatrixRoom>()
                .matching(CriteriaDefinition.empty())
                .then().awaitFirstOrNull()
    }

}