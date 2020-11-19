package net.folivo.matrix.bot.room

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.folivo.matrix.bot.config.MatrixBotDatabaseAutoconfiguration
import net.folivo.matrix.bot.membership.MatrixMembership
import net.folivo.matrix.bot.user.MatrixUser
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.delete

@DataR2dbcTest
@ImportAutoConfiguration(MatrixBotDatabaseAutoconfiguration::class)
class MatrixRoomRepositoryTest(
        cut: MatrixRoomRepository,
        db: R2dbcEntityTemplate
) : DescribeSpec(testBody(cut, db))

private fun testBody(cut: MatrixRoomRepository, db: R2dbcEntityTemplate): DescribeSpec.() -> Unit {
    return {
        val user1 = UserId("user1", "server")
        val user2 = UserId("user2", "server")
        val room1 = RoomId("room1", "server")
        val room2 = RoomId("room2", "server")
        val room3 = RoomId("room3", "server")


        beforeSpec {
            db.insert(MatrixUser(user1)).awaitFirstOrNull()
            db.insert(MatrixUser(user2)).awaitFirstOrNull()
            db.insert(MatrixRoom(room1)).awaitFirstOrNull()
            db.insert(MatrixRoom(room2)).awaitFirstOrNull()
            db.insert(MatrixRoom(room3)).awaitFirstOrNull()
            db.insert(MatrixMembership(user1, room1)).awaitFirstOrNull()
            db.insert(MatrixMembership(user2, room1)).awaitFirstOrNull()
            db.insert(MatrixMembership(user2, room2)).awaitFirstOrNull()
            db.insert(MatrixMembership(user1, room3)).awaitFirstOrNull()
            db.insert(MatrixMembership(user2, room3)).awaitFirstOrNull()
        }

        describe(MatrixRoomRepository::findByMembers.name) {
            it("should find matching room") {
                cut.findByMembers(setOf(user1, user2)).toList().map { it.id }
                        .shouldContainExactlyInAnyOrder(room1, room3)
                cut.findByMembers(setOf(user2)).toList().map { it.id }
                        .shouldContainExactlyInAnyOrder(room1, room2, room3)

            }
            it("should not find matching room") {
                cut.findByMembers(setOf(UserId("unknown", "server"))).toList().shouldBeEmpty()
            }
        }

        afterSpec {
            db.delete<MatrixMembership>().all().awaitFirstOrNull()
            db.delete<MatrixRoom>().all().awaitFirstOrNull()
            db.delete<MatrixUser>().all().awaitFirstOrNull()
        }
    }
}