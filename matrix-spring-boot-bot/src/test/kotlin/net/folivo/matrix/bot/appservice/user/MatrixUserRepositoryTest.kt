package net.folivo.matrix.bot.appservice.user

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlinx.coroutines.flow.toList
import net.folivo.matrix.bot.RepositoryTestHelper
import net.folivo.matrix.bot.appservice.membership.MatrixMembership
import net.folivo.matrix.bot.appservice.room.MatrixRoom
import net.folivo.matrix.bot.config.MatrixBotDatabaseAutoconfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.DatabaseClient

@DataR2dbcTest
@ImportAutoConfiguration(MatrixBotDatabaseAutoconfiguration::class)
class MatrixUserRepositoryTest(
        cut: MatrixUserRepository,
        dbClient: DatabaseClient
) : DescribeSpec(testBody(cut, dbClient))

private fun testBody(cut: MatrixUserRepository, dbClient: DatabaseClient): DescribeSpec.() -> Unit {
    return {
        val h = RepositoryTestHelper(dbClient)

        beforeSpec {
            h.deleteAllMemberships()
            h.deleteAllRooms()
            h.deleteAllUsers()

            h.insertUser(MatrixUser("userId1"))
            h.insertUser(MatrixUser("userId2"))
            h.insertUser(MatrixUser("userId3"))
            h.insertRoom(MatrixRoom("roomId1"))
            h.insertRoom(MatrixRoom("roomId2"))
            h.insertRoom(MatrixRoom("roomId3"))
            h.insertMembership(MatrixMembership("userId1", "roomId1"))
            h.insertMembership(MatrixMembership("userId2", "roomId1"))
            h.insertMembership(MatrixMembership("userId3", "roomId1"))
            h.insertMembership(MatrixMembership("userId1", "roomId2"))
            h.insertMembership(MatrixMembership("userId2", "roomId2"))
        }

        describe(MatrixUserRepository::findByRoomId.name) {
            it("should find users") {
                cut.findByRoomId("roomId1").toList().map { it.id }
                        .shouldContainExactlyInAnyOrder("userId1", "userId2", "userId3")
                cut.findByRoomId("roomId2").toList().map { it.id }
                        .shouldContainExactlyInAnyOrder("userId1", "userId2")
            }
            it("should not find users") {
                cut.findByRoomId("roomId3").toList().map { it.id }
                        .shouldBeEmpty()
            }
        }
    }
}