package net.folivo.matrix.bot.util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.mockk.every
import io.mockk.mockk
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.core.model.MatrixId.RoomAliasId
import net.folivo.matrix.core.model.MatrixId.UserId

class BotServiceHelperTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val botPropertiesMock: MatrixBotProperties = mockk {
            every { serverName } returns "server"
            every { username } returns "bot"
        }

        val cut = BotServiceHelper(botPropertiesMock, setOf(Regex("unicorn_.+")), setOf(Regex("dino_.+")))

        describe(BotServiceHelper::isManagedUser.name) {
            it("it should return true when userId is bot") {
                cut.isManagedUser(UserId("@bot:server")).shouldBeTrue()
            }
            it("it should return true when userId is in namespace") {
                cut.isManagedUser(UserId("@unicorn_fluffy:server")).shouldBeTrue()
            }
            it("it should return false when userId is not in namespace") {
                cut.isManagedUser(UserId("@cat_fluffy:server")).shouldBeFalse()
            }
        }
        describe(BotServiceHelper::isManagedRoom.name) {
            it("it should return true when room alias is in namespace") {
                cut.isManagedRoom(RoomAliasId("#dino_large:server")).shouldBeTrue()
            }
            it("it should return false when room alias is not in namespace") {
                cut.isManagedRoom(RoomAliasId("#cat_buhu:server")).shouldBeFalse()
            }
        }
    }
}