package net.folivo.matrix.bot.appservice.membership

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList
import net.folivo.matrix.bot.RepositoryTestHelper
import net.folivo.matrix.bot.appservice.room.MatrixRoom
import net.folivo.matrix.bot.appservice.user.MatrixUser
import net.folivo.matrix.bot.config.MatrixBotDatabaseAutoconfiguration
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.DatabaseClient

@DataR2dbcTest
@ImportAutoConfiguration(MatrixBotDatabaseAutoconfiguration::class)
class MatrixMembershipRepositoryTest(
        cut: MatrixMembershipRepository,
        dbClient: DatabaseClient
) : DescribeSpec(testBody(cut, dbClient))

private fun testBody(cut: MatrixMembershipRepository, dbClient: DatabaseClient): DescribeSpec.() -> Unit {
    return {
        val h = RepositoryTestHelper(dbClient)

        val userId1 = UserId("user1", "server")
        val userId2 = UserId("user2", "server")
        val roomId1 = RoomId("room1", "server")
        val roomId2 = RoomId("room2", "server")

        beforeSpec {
            h.insertUser(MatrixUser(userId1))
            h.insertUser(MatrixUser(userId2))
            h.insertRoom(MatrixRoom(roomId1))
            h.insertRoom(MatrixRoom(roomId2))
            h.insertMembership(MatrixMembership(userId1, roomId1))
            h.insertMembership(MatrixMembership(userId2, roomId1))
            h.insertMembership(MatrixMembership(userId2, roomId2))
        }

        describe(MatrixMembershipRepository::findByRoomId.name) {
            it("should return multiple memberships") {
                cut.findByRoomId(roomId1).toList().map { it.userId to it.roomId }
                        .shouldContainExactlyInAnyOrder(
                                userId1 to roomId1,
                                userId2 to roomId1
                        )
            }
            it("should return no memberships") {
                cut.findByRoomId(RoomId("unknown", "blub")).toList().shouldBeEmpty()
            }
        }
        describe(MatrixMembershipRepository::findByUserId.name) {
            it("should return multiple memberships") {
                cut.findByUserId(userId2).toList().map { it.userId to it.roomId }
                        .shouldContainExactlyInAnyOrder(
                                userId2 to roomId1,
                                userId2 to roomId2
                        )
            }
            it("should return no memberships") {
                cut.findByUserId(UserId("unknown", "blub")).toList().shouldBeEmpty()
            }
        }
        describe(MatrixMembershipRepository::countByRoomId.name) {
            it("should count multiple memberships") {
                cut.countByRoomId(roomId1).shouldBe(2L)
            }
            it("should count no memberships") {
                cut.countByRoomId(RoomId("unknown", "blub")).shouldBe(0L)
            }
        }
        describe(MatrixMembershipRepository::countByUserId.name) {
            it("should count multiple memberships") {
                cut.countByUserId(userId2).shouldBe(2L)
            }
            it("should count no memberships") {
                cut.countByUserId(UserId("unknown", "blub")).shouldBe(0L)
            }
        }
        describe(MatrixMembershipRepository::findByUserIdAndRoomId.name) {
            it("should return one membership") {
                cut.findByUserIdAndRoomId(userId1, roomId1)
                        .let { it?.userId to it?.roomId }
                        .shouldBe(userId1 to roomId1)
            }
            it("should return no membership") {
                cut.findByUserIdAndRoomId(UserId("unknown", "blub"), roomId1).shouldBeNull()
            }
        }
        describe(MatrixMembershipRepository::deleteByUserIdAndRoomId.name) {
            it("should delete membership") {
                h.insertMembership(MatrixMembership(userId1, roomId2))
                cut.deleteByUserIdAndRoomId(userId1, roomId2)
                cut.findByUserIdAndRoomId(userId1, roomId2).shouldBeNull()
            }
            it("should do nothing") {
                cut.deleteByUserIdAndRoomId(UserId("unknown", "blub"), roomId1)
            }
        }
        describe(MatrixMembershipRepository::containsMembersByRoomId.name) {
            it("should contain members in room") {
                cut.containsMembersByRoomId(roomId1, setOf(userId1, userId2)).shouldBeTrue()
                cut.containsMembersByRoomId(roomId1, setOf(userId2)).shouldBeTrue()
            }
            it("should not contain members in room") {
                cut.containsMembersByRoomId(roomId1, setOf(UserId("unknown", "blub"))).shouldBeFalse()
                cut.containsMembersByRoomId(RoomId("unknown", "blub"), setOf(userId1)).shouldBeFalse()
                cut.containsMembersByRoomId(roomId1, setOf(userId1, UserId("unknown", "blub"))).shouldBeFalse()
            }
        }
        describe(MatrixMembershipRepository::containsOnlyManagedMembersByRoomId.name) {
            it("should contain only managed members") {
                val managedUser1 = UserId("managed1", "server")
                val managedUser2 = UserId("managed2", "server")
                val managedRoom = RoomId("managed", "server")
                h.insertUser(MatrixUser(managedUser1, true))
                h.insertUser(MatrixUser(managedUser2, true))
                h.insertRoom(MatrixRoom(managedRoom))
                h.insertMembership(MatrixMembership(managedUser1, managedRoom))
                h.insertMembership(MatrixMembership(managedUser2, managedRoom))

                cut.containsOnlyManagedMembersByRoomId(managedRoom).shouldBeTrue()
            }
            it("should not contain only managed members") {
                cut.containsOnlyManagedMembersByRoomId(roomId1).shouldBeFalse()
            }
        }

        afterSpec {
            h.deleteAllMemberships()
            h.deleteAllRooms()
            h.deleteAllUsers()
        }
    }
}