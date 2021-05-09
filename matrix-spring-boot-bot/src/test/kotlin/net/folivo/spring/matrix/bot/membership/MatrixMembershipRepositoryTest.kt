package net.folivo.spring.matrix.bot.membership

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.folivo.spring.matrix.bot.config.MatrixBotDatabaseAutoconfiguration
import net.folivo.spring.matrix.bot.room.MatrixRoom
import net.folivo.spring.matrix.bot.user.MatrixUser
import net.folivo.trixnity.core.model.MatrixId
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.delete

@DataR2dbcTest
@ImportAutoConfiguration(MatrixBotDatabaseAutoconfiguration::class)
class MatrixMembershipRepositoryTest(
    cut: MatrixMembershipRepository,
    db: R2dbcEntityTemplate
) : DescribeSpec(testBody(cut, db))

private fun testBody(cut: MatrixMembershipRepository, db: R2dbcEntityTemplate): DescribeSpec.() -> Unit {
    return {
        val userId1 = MatrixId.UserId("user1", "server")
        val userId2 = MatrixId.UserId("user2", "server")
        val roomId1 = MatrixId.RoomId("room1", "server")
        val roomId2 = MatrixId.RoomId("room2", "server")

        beforeSpec {
            db.insert(MatrixUser(userId1)).awaitFirstOrNull()
            db.insert(MatrixUser(userId2)).awaitFirstOrNull()
            db.insert(MatrixRoom(roomId1)).awaitFirstOrNull()
            db.insert(MatrixRoom(roomId2)).awaitFirstOrNull()
            db.insert(MatrixMembership(userId1, roomId1)).awaitFirstOrNull()
            db.insert(MatrixMembership(userId2, roomId1)).awaitFirstOrNull()
            db.insert(MatrixMembership(userId2, roomId2)).awaitFirstOrNull()
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
                cut.findByRoomId(MatrixId.RoomId("unknown", "blub")).toList().shouldBeEmpty()
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
                cut.findByUserId(MatrixId.UserId("unknown", "blub")).toList().shouldBeEmpty()
            }
        }
        describe(MatrixMembershipRepository::countByRoomId.name) {
            it("should count multiple memberships") {
                cut.countByRoomId(roomId1).shouldBe(2L)
            }
            it("should count no memberships") {
                cut.countByRoomId(MatrixId.RoomId("unknown", "blub")).shouldBe(0L)
            }
        }
        describe(MatrixMembershipRepository::countByUserId.name) {
            it("should count multiple memberships") {
                cut.countByUserId(userId2).shouldBe(2L)
            }
            it("should count no memberships") {
                cut.countByUserId(MatrixId.UserId("unknown", "blub")).shouldBe(0L)
            }
        }
        describe(MatrixMembershipRepository::findByUserIdAndRoomId.name) {
            it("should return one membership") {
                cut.findByUserIdAndRoomId(userId1, roomId1)
                    .let { it?.userId to it?.roomId }
                    .shouldBe(userId1 to roomId1)
            }
            it("should return no membership") {
                cut.findByUserIdAndRoomId(MatrixId.UserId("unknown", "blub"), roomId1).shouldBeNull()
            }
        }
        describe(MatrixMembershipRepository::deleteByUserIdAndRoomId.name) {
            it("should delete membership") {
                db.insert(MatrixMembership(userId1, roomId2)).awaitFirstOrNull()
                cut.deleteByUserIdAndRoomId(userId1, roomId2)
                cut.findByUserIdAndRoomId(userId1, roomId2).shouldBeNull()
            }
            it("should do nothing") {
                cut.deleteByUserIdAndRoomId(MatrixId.UserId("unknown", "blub"), roomId1)
            }
        }
        describe(MatrixMembershipRepository::containsMembersByRoomId.name) {
            it("should contain members in room") {
                cut.containsMembersByRoomId(roomId1, setOf(userId1, userId2)).shouldBeTrue()
                cut.containsMembersByRoomId(roomId1, setOf(userId2)).shouldBeTrue()
            }
            it("should not contain members in room") {
                cut.containsMembersByRoomId(roomId1, setOf(MatrixId.UserId("unknown", "blub"))).shouldBeFalse()
                cut.containsMembersByRoomId(MatrixId.RoomId("unknown", "blub"), setOf(userId1)).shouldBeFalse()
                cut.containsMembersByRoomId(roomId1, setOf(userId1, MatrixId.UserId("unknown", "blub"))).shouldBeFalse()
            }
        }
        describe(MatrixMembershipRepository::containsOnlyManagedMembersByRoomId.name) {
            it("should contain only managed members") {
                val managedUser1 = MatrixId.UserId("managed1", "server")
                val managedUser2 = MatrixId.UserId("managed2", "server")
                val managedRoom = MatrixId.RoomId("managed", "server")
                db.insert(MatrixUser(managedUser1, true)).awaitFirstOrNull()
                db.insert(MatrixUser(managedUser2, true)).awaitFirstOrNull()
                db.insert(MatrixRoom(managedRoom)).awaitFirstOrNull()
                db.insert(MatrixMembership(managedUser1, managedRoom)).awaitFirstOrNull()
                db.insert(MatrixMembership(managedUser2, managedRoom)).awaitFirstOrNull()

                cut.containsOnlyManagedMembersByRoomId(managedRoom).shouldBeTrue()
            }
            it("should not contain only managed members") {
                cut.containsOnlyManagedMembersByRoomId(roomId1).shouldBeFalse()
            }
        }

        afterSpec {
            db.delete<MatrixMembership>().all().awaitFirstOrNull()
            db.delete<MatrixRoom>().all().awaitFirstOrNull()
            db.delete<MatrixUser>().all().awaitFirstOrNull()
        }
    }
}