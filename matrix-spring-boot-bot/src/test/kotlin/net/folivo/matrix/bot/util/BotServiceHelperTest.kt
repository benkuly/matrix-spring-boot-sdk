package net.folivo.matrix.bot.util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.folivo.matrix.appservice.config.AppserviceProperties
import net.folivo.matrix.appservice.config.AppserviceProperties.Namespace
import net.folivo.matrix.bot.config.MatrixBotProperties

class BotServiceHelperTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val botPropertiesMock: MatrixBotProperties = mockk {
            every { serverName } returns "server"
            every { username } returns "bot"
        }
        val appservicePropertiesMock: AppserviceProperties = mockk {
            every { namespaces.users } returns listOf(Namespace("@unicorn_.+"))
            every { namespaces.rooms } returns listOf(Namespace("#dino_.+"))
        }

        val cut = BotServiceHelper(botPropertiesMock, appservicePropertiesMock)

        describe(BotServiceHelper::getBotUserId.name) {
            it("should return bot userId") {
                cut.getBotUserId().shouldBe("@bot:server")
            }
        }
        describe(BotServiceHelper::isManagedUser.name) {
            it("it should return true when userId is bot") {
                cut.isManagedUser("@bot:server").shouldBeTrue()
            }
            it("it should return true when userId is in namespace") {
                cut.isManagedUser("@unicorn_fluffy:server").shouldBeTrue()
            }
            it("it should return false when userId is not in namespace") {
                cut.isManagedUser("@cat_fluffy:server").shouldBeFalse()
            }
        }
        describe(BotServiceHelper::isManagedRoom.name) {
            it("it should return true when room alias is in namespace") {
                cut.isManagedRoom("#dino_large:server").shouldBeTrue()
            }
            it("it should return false when room alias is not in namespace") {
                cut.isManagedRoom("#cat_buhu:server").shouldBeFalse()
            }
        }
    }
}