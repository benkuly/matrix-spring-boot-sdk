package net.folivo.matrix.bot.appservice.user

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.bot.util.BotServiceHelper

class BotUserInitializerTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val appserviceHandlerHelperMock: AppserviceHandlerHelper = mockk(relaxed = true)
        val botServiceHelperMock: BotServiceHelper = mockk(relaxed = true)

        val cut = BotUserInitializer(appserviceHandlerHelperMock, botServiceHelperMock)

        describe(BotUserInitializer::initializeBotUser.name) {
            it("should initialize bot user") {
                coEvery { botServiceHelperMock.getBotUserId() }.returns("@bot:server")
                cut.initializeBotUser()

                coVerify {
                    appserviceHandlerHelperMock.registerManagedUser("@bot:server")
                }
            }
        }
    }
}