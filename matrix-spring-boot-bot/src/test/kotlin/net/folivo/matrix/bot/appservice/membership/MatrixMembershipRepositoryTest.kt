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

        beforeSpec {
            h.insertUser(MatrixUser("userId1"))
            h.insertUser(MatrixUser("userId2"))
            h.insertRoom(MatrixRoom("roomId1"))
            h.insertRoom(MatrixRoom("roomId2"))
            h.insertMembership(MatrixMembership("userId1", "roomId1"))
            h.insertMembership(MatrixMembership("userId2", "roomId1"))
            h.insertMembership(MatrixMembership("userId2", "roomId2"))
        }

        describe(MatrixMembershipRepository::findByRoomId.name) {
            it("should return multiple memberships") {
                cut.findByRoomId("roomId1").toList().map { it.userId to it.roomId }
                        .shouldContainExactlyInAnyOrder(
                                "userId1" to "roomId1",
                                "userId2" to "roomId1"
                        )
            }
            it("should return no memberships") {
                cut.findByRoomId("unknownRoom").toList().shouldBeEmpty()
            }
        }
        describe(MatrixMembershipRepository::findByUserId.name) {
            it("should return multiple memberships") {
                cut.findByUserId("userId2").toList().map { it.userId to it.roomId }
                        .shouldContainExactlyInAnyOrder(
                                "userId2" to "roomId1",
                                "userId2" to "roomId2"
                        )
            }
            it("should return no memberships") {
                cut.findByUserId("unknownUser").toList().shouldBeEmpty()
            }
        }
        describe(MatrixMembershipRepository::countByRoomId.name) {
            it("should count multiple memberships") {
                cut.countByRoomId("roomId1").shouldBe(2L)
            }
            it("should count no memberships") {
                cut.countByRoomId("unknownRoom").shouldBe(0L)
            }
        }
        describe(MatrixMembershipRepository::countByUserId.name) {
            it("should count multiple memberships") {
                cut.countByUserId("userId2").shouldBe(2L)
            }
            it("should count no memberships") {
                cut.countByUserId("unknownUser").shouldBe(0L)
            }
        }
        describe(MatrixMembershipRepository::findByUserIdAndRoomId.name) {
            it("should return one membership") {
                cut.findByUserIdAndRoomId("userId1", "roomId1")
                        .let { it?.userId to it?.roomId }
                        .shouldBe("userId1" to "roomId1")
            }
            it("should return no membership") {
                cut.findByUserIdAndRoomId("unknownUser", "roomId1").shouldBeNull()
            }
        }
        describe(MatrixMembershipRepository::deleteByUserIdAndRoomId.name) {
            it("should delete membership") {
                h.insertMembership(MatrixMembership("userId1", "roomId2"))
                cut.deleteByUserIdAndRoomId("userId1", "roomId2")
                cut.findByUserIdAndRoomId("userId1", "userId2").shouldBeNull()
            }
            it("should do nothing") {
                cut.deleteByUserIdAndRoomId("unknownUser", "roomId1")
            }
        }
        describe(MatrixMembershipRepository::containsMembersByRoomId.name) {
            it("should contain members in room") {
                cut.containsMembersByRoomId("roomId1", setOf("userId1", "userId2")).shouldBeTrue()
                cut.containsMembersByRoomId("roomId1", setOf("userId2")).shouldBeTrue()
            }
            it("should not contain members in room") {
                cut.containsMembersByRoomId("roomId1", setOf("unknownUser")).shouldBeFalse()
                cut.containsMembersByRoomId("unknownRoom", setOf("userId1")).shouldBeFalse()
                cut.containsMembersByRoomId("roomId1", setOf("userId1", "unknownUser")).shouldBeFalse()
            }
        }
        describe(MatrixMembershipRepository::containsOnlyManagedMembersByRoomId.name) {
            it("should contain only managed members") {
                h.insertUser(MatrixUser("managedUserId1", true))
                h.insertUser(MatrixUser("managedUserId2", true))
                h.insertRoom(MatrixRoom("managedUserRoomId"))
                h.insertMembership(MatrixMembership("managedUserId1", "managedUserRoomId"))
                h.insertMembership(MatrixMembership("managedUserId2", "managedUserRoomId"))

                cut.containsOnlyManagedMembersByRoomId("managedUserRoomId").shouldBeTrue()
            }
            it("should not contain only managed members") {
                cut.containsOnlyManagedMembersByRoomId("roomId1").shouldBeFalse()
            }
        }

        afterSpec {
            h.deleteAllMemberships()
            h.deleteAllRooms()
            h.deleteAllUsers()
        }
    }
}