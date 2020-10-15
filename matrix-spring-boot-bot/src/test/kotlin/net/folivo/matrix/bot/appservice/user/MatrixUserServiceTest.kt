package net.folivo.matrix.bot.appservice.user

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.matrix.bot.util.BotServiceHelper

class MatrixUserServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val userRepositoryMock: MatrixUserRepository = mockk(relaxed = true)
        val helperMock: BotServiceHelper = mockk(relaxed = true)

        val cut = MatrixUserService(userRepositoryMock, helperMock)

        describe(MatrixUserService::getOrCreateUser.name) {
            val user = MatrixUser("userId")

            it("should save new user when user does not exist") {
                coEvery { userRepositoryMock.findById(any<String>()) }.returns(null)
                coEvery { userRepositoryMock.save(any()) }.returns(user)
                cut.getOrCreateUser("userId").shouldBe(user)
                coVerify { userRepositoryMock.save(user) }
            }
            it("should use existing room") {
                coEvery { userRepositoryMock.findById(any<String>()) }.returns(user)
                cut.getOrCreateUser("userId").shouldBe(user)
                coVerify(exactly = 0) { userRepositoryMock.save(any()) }
            }
        }
    }
}