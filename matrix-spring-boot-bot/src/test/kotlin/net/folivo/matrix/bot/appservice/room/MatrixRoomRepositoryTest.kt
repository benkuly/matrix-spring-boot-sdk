package net.folivo.matrix.bot.appservice.room

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlinx.coroutines.flow.toList
import net.folivo.matrix.bot.RepositoryTestHelper
import net.folivo.matrix.bot.appservice.membership.MatrixMembership
import net.folivo.matrix.bot.appservice.user.MatrixUser
import net.folivo.matrix.bot.config.MatrixBotDatabaseAutoconfiguration
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

        beforeSpec {
            h.deleteAllMemberships()
            h.deleteAllRooms()
            h.deleteAllUsers()

            h.insertUser(MatrixUser("userId1"))
            h.insertUser(MatrixUser("userId2"))
            h.insertRoom(MatrixRoom("roomId1"))
            h.insertRoom(MatrixRoom("roomId2"))
            h.insertRoom(MatrixRoom("roomId3"))
            h.insertMembership(MatrixMembership("userId1", "roomId1"))
            h.insertMembership(MatrixMembership("userId2", "roomId1"))
            h.insertMembership(MatrixMembership("userId2", "roomId2"))
            h.insertMembership(MatrixMembership("userId1", "roomId3"))
            h.insertMembership(MatrixMembership("userId2", "roomId3"))
        }

        describe(MatrixRoomRepository::findByMembers.name) {
            it("should find matching room") {
                cut.findByMembers(setOf("userId1", "userId2")).toList().map { it.id }
                        .shouldContainExactlyInAnyOrder("roomId1", "roomId3")
                cut.findByMembers(setOf("userId2")).toList().map { it.id }
                        .shouldContainExactlyInAnyOrder("roomId1", "roomId2", "roomId3")

            }
            it("should not find matching room") {
                cut.findByMembers(setOf("unknownUser")).toList().shouldBeEmpty()
            }
        }
    }
}