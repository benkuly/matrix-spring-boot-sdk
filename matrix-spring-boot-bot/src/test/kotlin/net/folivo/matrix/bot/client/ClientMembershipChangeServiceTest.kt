package net.folivo.matrix.bot.client

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue

class ClientMembershipChangeServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val cut = ClientMembershipChangeService()
        
        describe(ClientMembershipChangeService::shouldJoinRoom.name) {
            it("should always return true") {
                cut.shouldJoinRoom("bla", "bla").shouldBeTrue()
            }
        }
    }
}