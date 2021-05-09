package net.folivo.spring.matrix.bot.appservice

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.spring.matrix.bot.config.MatrixBotProperties
import net.folivo.spring.matrix.bot.user.MatrixUserService
import net.folivo.spring.matrix.bot.util.BotServiceHelper
import net.folivo.trixnity.appservice.rest.room.CreateRoomParameter
import net.folivo.trixnity.appservice.rest.user.AppserviceUserService.UserExistingState.*
import net.folivo.trixnity.appservice.rest.user.RegisterUserParameter
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.core.model.MatrixId

class DefaultAppserviceUserServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val userServiceMock: MatrixUserService = mockk(relaxed = true)
        val helperMock: BotServiceHelper = mockk(relaxed = true)
        val botPropertiesMock: MatrixBotProperties = mockk(relaxed = true)
        val matrixClientMock: MatrixClient = mockk(relaxed = true)

        val cut = DefaultAppserviceUserService(userServiceMock, helperMock, botPropertiesMock, matrixClientMock)

        val userId = MatrixId.UserId("user", "server")
        val botUserId = MatrixId.UserId("bot", "server")

        describe(DefaultAppserviceUserService::userExistingState.name) {
            it("should return $EXISTS when user id does exists") {
                coEvery { userServiceMock.existsUser(userId) }.returns(true)
                cut.userExistingState(userId).shouldBe(EXISTS)
            }
            it("should return $CAN_BE_CREATED when user id does not exists but is managed") {
                coEvery { userServiceMock.existsUser(userId) }.returns(false)
                coEvery { helperMock.isManagedUser(userId) }.returns(true)
                cut.userExistingState(userId).shouldBe(CAN_BE_CREATED)
            }
            it("should return $DOES_NOT_EXISTS when user id does not exists and is not managed") {
                coEvery { userServiceMock.existsUser(userId) }.returns(false)
                coEvery { helperMock.isManagedUser(userId) }.returns(false)
                cut.userExistingState(userId).shouldBe(DOES_NOT_EXISTS)
            }
        }

        describe(DefaultAppserviceUserService::getRegisterUserParameter.name) {
            it("should return displaname when bot") {
                coEvery { botPropertiesMock.botUserId }.returns(botUserId)
                coEvery { botPropertiesMock.displayName }.returns("BOT")
                cut.getRegisterUserParameter(botUserId).shouldBe(RegisterUserParameter(displayName = "BOT"))
            }
            it("should return empty ${CreateRoomParameter::class.simpleName}") {
                coEvery { botPropertiesMock.botUserId }.returns(botUserId)
                cut.getRegisterUserParameter(userId).shouldBe(RegisterUserParameter())
            }
        }

        describe(DefaultAppserviceUserService::onRegisteredUser.name) {
            it("should save room alias") {
                cut.onRegisteredUser(userId)
                coVerify { userServiceMock.getOrCreateUser(userId) }
            }
        }
    }
}