package net.folivo.matrix.bot.client

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId

class ClientMembershipChangeServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val cut = ClientMembershipChangeService()

        describe(ClientMembershipChangeService::shouldJoinRoom.name) {
            it("should always return true") {
                cut.shouldJoinRoom(UserId("user", "server"), RoomId("room", "server")).shouldBeTrue()
            }
        }
    }
}