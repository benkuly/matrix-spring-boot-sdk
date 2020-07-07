package net.folivo.matrix.bot.appservice

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import net.folivo.matrix.appservice.api.user.CreateUserParameter
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.user.RegisterResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND

@ExtendWith(MockKExtension::class)
class BotUserInitializerTest {
    @MockK
    lateinit var matrixClient: MatrixClient

    @MockK
    lateinit var botProperties: MatrixBotProperties

    @MockK
    lateinit var userService: MatrixAppserviceUserService

    @InjectMockKs
    lateinit var cut: BotUserInitializer

    @BeforeEach
    fun beforeEach() {
        every { botProperties.username }.returns("bot")
        every { botProperties.serverName }.returns("server")
        coEvery { userService.saveUser(any()) } just Runs
        coEvery { userService.getCreateUserParameter(any()) }.returns(CreateUserParameter())
        coEvery { matrixClient.userApi.register(any(), any(), any(), any(), any(), any(), any(), any()) }
                .returns(RegisterResponse("someUserId"))
        coEvery { matrixClient.userApi.setDisplayName(any(), any(), any()) } just Runs
    }

    @Test
    fun `should register bot user`() {
        runBlocking { cut.initializeBotUserAsync() }

        coVerifyAll {
            matrixClient.userApi.register(
                    authenticationType = "m.login.application_service",
                    username = "bot"
            )

            userService.saveUser("@bot:server")
            userService.getCreateUserParameter("@bot:server")
        }
        coVerify(inverse = true) { matrixClient.userApi.setDisplayName(any(), any()) }
    }

    @Test
    fun `should set displayname`() {
        coEvery { userService.getCreateUserParameter(any()) }.returns(CreateUserParameter("DINO"))

        runBlocking { cut.initializeBotUserAsync() }

        coVerifyAll {
            matrixClient.userApi.register(
                    authenticationType = "m.login.application_service",
                    username = "bot"
            )

            userService.saveUser("@bot:server")
            userService.getCreateUserParameter("@bot:server")
            matrixClient.userApi.setDisplayName("@bot:server", "DINO")
        }
    }

    @Test
    fun `should catch already registered error`() {
        coEvery { matrixClient.userApi.register(any(), any(), any(), any(), any(), any(), any(), any()) }
                .throws(MatrixServerException(FORBIDDEN, ErrorResponse("M_USER_IN_USE")))
        runBlocking { cut.initializeBotUserAsync() }

        coVerifyAll {
            matrixClient.userApi.register(
                    authenticationType = "m.login.application_service",
                    username = "bot"
            )
            userService.saveUser("@bot:server")
            userService.getCreateUserParameter("@bot:server")
        }
    }

    @Test
    fun `should fail when unexpected error`() {
        coEvery { matrixClient.userApi.register(any(), any(), any(), any(), any(), any(), any(), any()) }
                .throws(MatrixServerException(NOT_FOUND, ErrorResponse("M_UNKNOWN")))
        try {
            runBlocking { cut.initializeBotUserAsync() }
            fail { "should have error" }
        } catch (error: Throwable) {

        }

        coVerify {
            matrixClient.userApi.register(
                    authenticationType = "m.login.application_service",
                    username = "bot"
            )
        }
        coVerifyAll(inverse = true) {
            matrixClient.userApi.setDisplayName("@bot:server", "DINO")
            userService.saveUser("@bot:server")
            userService.getCreateUserParameter("@bot:server")
        }
    }
}