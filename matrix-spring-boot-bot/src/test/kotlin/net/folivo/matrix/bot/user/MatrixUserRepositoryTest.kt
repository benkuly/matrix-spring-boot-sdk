package net.folivo.matrix.bot.user

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.folivo.matrix.bot.config.MatrixBotDatabaseAutoconfiguration
import net.folivo.matrix.bot.membership.MatrixMembership
import net.folivo.matrix.bot.room.MatrixRoom
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.delete

@DataR2dbcTest
@ImportAutoConfiguration(MatrixBotDatabaseAutoconfiguration::class)
class MatrixUserRepositoryTest(
        cut: MatrixUserRepository,
        db: R2dbcEntityTemplate
) : DescribeSpec(testBody(cut, db))

private fun testBody(cut: MatrixUserRepository, db: R2dbcEntityTemplate): DescribeSpec.() -> Unit {
    return {
        val userId1 = UserId("user1", "server")
        val userId2 = UserId("user2", "server")
        val userId3 = UserId("user3", "server")
        val roomId1 = RoomId("room1", "server")
        val roomId2 = RoomId("room2", "server")
        val roomId3 = RoomId("room3", "server")

        beforeSpec {
            db.insert(MatrixUser(userId1)).awaitFirstOrNull()
            db.insert(MatrixUser(userId2)).awaitFirstOrNull()
            db.insert(MatrixUser(userId3)).awaitFirstOrNull()
            db.insert(MatrixRoom(roomId1)).awaitFirstOrNull()
            db.insert(MatrixRoom(roomId2)).awaitFirstOrNull()
            db.insert(MatrixRoom(roomId3)).awaitFirstOrNull()
            db.insert(MatrixMembership(userId1, roomId1)).awaitFirstOrNull()
            db.insert(MatrixMembership(userId2, roomId1)).awaitFirstOrNull()
            db.insert(MatrixMembership(userId3, roomId1)).awaitFirstOrNull()
            db.insert(MatrixMembership(userId1, roomId2)).awaitFirstOrNull()
            db.insert(MatrixMembership(userId2, roomId2)).awaitFirstOrNull()
        }

        describe(MatrixUserRepository::findByRoomId.name) {
            it("should find users") {
                cut.findByRoomId(roomId1).toList().map { it.id }
                        .shouldContainExactlyInAnyOrder(userId1, userId2, userId3)
                cut.findByRoomId(roomId2).toList().map { it.id }
                        .shouldContainExactlyInAnyOrder(userId1, userId2)
            }
            it("should not find users") {
                cut.findByRoomId(roomId3).toList().map { it.id }
                        .shouldBeEmpty()
            }
        }

        afterSpec {
            db.delete<MatrixMembership>().all().awaitFirstOrNull()
            db.delete<MatrixRoom>().all().awaitFirstOrNull()
            db.delete<MatrixUser>().all().awaitFirstOrNull()
        }
    }
}