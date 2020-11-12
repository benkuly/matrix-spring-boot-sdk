package net.folivo.matrix.bot.appservice

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.core.model.MatrixId.UserId

class BotUserInitializerTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val appserviceHandlerHelperMock: AppserviceHandlerHelper = mockk(relaxed = true)
        val botPropertiesMock: MatrixBotProperties = mockk()

        val cut = BotUserInitializer(appserviceHandlerHelperMock, botPropertiesMock)

        describe(BotUserInitializer::initializeBotUser.name) {
            it("should initialize bot user") {
                coEvery { botPropertiesMock.botUserId }.returns(UserId("@bot:server"))
                cut.initializeBotUser()

                coVerify {
                    appserviceHandlerHelperMock.registerManagedUser(UserId("@bot:server"))
                }
            }
        }
    }
}