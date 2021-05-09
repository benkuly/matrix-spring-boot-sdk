package net.folivo.spring.matrix.bot.appservice

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.spring.matrix.bot.config.MatrixBotProperties
import net.folivo.trixnity.appservice.rest.user.AppserviceUserService
import net.folivo.trixnity.core.model.MatrixId

class BotUserInitializerTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val appserviceUserServiceMock: AppserviceUserService = mockk(relaxed = true)
        val botPropertiesMock: MatrixBotProperties = mockk()

        val cut = BotUserInitializer(appserviceUserServiceMock, botPropertiesMock)

        describe(BotUserInitializer::initializeBotUser.name) {
            it("should initialize bot user") {
                coEvery { botPropertiesMock.botUserId }.returns(MatrixId.UserId("@bot:server"))
                cut.initializeBotUser()

                coVerify {
                    appserviceUserServiceMock.registerManagedUser(MatrixId.UserId("@bot:server"))
                }
            }
        }
    }
}