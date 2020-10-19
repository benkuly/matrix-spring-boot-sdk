package net.folivo.matrix.bot.room

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlinx.coroutines.flow.toList
import net.folivo.matrix.bot.RepositoryTestHelper
import net.folivo.matrix.bot.config.MatrixBotDatabaseAutoconfiguration
import net.folivo.matrix.bot.membership.MatrixMembership
import net.folivo.matrix.bot.user.MatrixUser
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.DatabaseClient

@DataR2dbcTest
@ImportAutoConfiguration(MatrixBotDatabaseAutoconfiguration::class)
class MatrixRoomRepositoryTest(
        cut: MatrixRoomRepository,
        dbClient: DatabaseClient
) : DescribeSpec(testBody(cut, dbClient))

private fun testBody(cut: MatrixRoomRepository, dbClient: DatabaseClient): DescribeSpec.() -> Unit {
    return {
        val h = RepositoryTestHelper(dbClient)

        val user1 = UserId("user1", "server")
        val user2 = UserId("user2", "server")
        val room1 = RoomId("room1", "server")
        val room2 = RoomId("room2", "server")
        val room3 = RoomId("room3", "server")


        beforeSpec {
            h.insertUser(MatrixUser(user1))
            h.insertUser(MatrixUser(user2))
            h.insertRoom(MatrixRoom(room1))
            h.insertRoom(MatrixRoom(room2))
            h.insertRoom(MatrixRoom(room3))
            h.insertMembership(MatrixMembership(user1, room1))
            h.insertMembership(MatrixMembership(user2, room1))
            h.insertMembership(MatrixMembership(user2, room2))
            h.insertMembership(MatrixMembership(user1, room3))
            h.insertMembership(MatrixMembership(user2, room3))
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
            h.deleteAllMemberships()
            h.deleteAllRooms()
            h.deleteAllUsers()
        }
    }
}