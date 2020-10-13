package net.folivo.matrix.bot.appservice.user

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.user.AppserviceUserService.UserExistingState.*
import net.folivo.matrix.appservice.api.user.RegisterUserParameter
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.util.BotServiceHelper

class DefaultAppserviceUserServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val userServiceMock: MatrixUserService = mockk(relaxed = true)
        val helperMock: BotServiceHelper = mockk(relaxed = true)
        val botPropertiesMock: MatrixBotProperties = mockk(relaxed = true)

        val cut = DefaultAppserviceUserService(userServiceMock, helperMock, botPropertiesMock)

        describe(DefaultAppserviceUserService::userExistingState.name) {
            it("should return $EXISTS when user id does exists") {
                coEvery { userServiceMock.existsUser("userId") }.returns(true)
                cut.userExistingState("userId").shouldBe(EXISTS)
            }
            it("should return $CAN_BE_CREATED when user id does not exists but is managed") {
                coEvery { userServiceMock.existsUser("userId") }.returns(false)
                coEvery { helperMock.isManagedUser("userId") }.returns(true)
                cut.userExistingState("userId").shouldBe(CAN_BE_CREATED)
            }
            it("should return $DOES_NOT_EXISTS when user id does not exists and is not managed") {
                coEvery { userServiceMock.existsUser("userId") }.returns(false)
                coEvery { helperMock.isManagedUser("userId") }.returns(false)
                cut.userExistingState("userId").shouldBe(DOES_NOT_EXISTS)
            }
        }

        describe(DefaultAppserviceUserService::getRegisterUserParameter.name) {
            it("should return displaname when bot") {
                coEvery { helperMock.getBotUserId() }.returns("@bot:server")
                coEvery { botPropertiesMock.displayName }.returns("BOT")
                cut.getRegisterUserParameter("@bot:server").shouldBe(RegisterUserParameter(displayName = "BOT"))
            }
            it("should return empty ${CreateRoomParameter::class}") {
                coEvery { helperMock.getBotUserId() }.returns("@bot:server")
                cut.getRegisterUserParameter("bla").shouldBe(RegisterUserParameter())
            }
        }

        describe(DefaultAppserviceUserService::onRegisteredUser.name) {
            it("should save room alias") {
                cut.onRegisteredUser("someUserId")
                coVerify { userServiceMock.getOrCreateUser("someUserId") }
            }
        }
    }
}