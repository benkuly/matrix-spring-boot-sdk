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

            it("should save new user when user does not exist") {
                val user1 = MatrixUser("userId1", true)
                val user2 = MatrixUser("userId2", false)

                coEvery { userRepositoryMock.findById(any<String>()) }.returns(null)
                coEvery { userRepositoryMock.save(user1) }.returns(user1)
                coEvery { userRepositoryMock.save(user2) }.returns(user2)
                coEvery { helperMock.isManagedUser("userId1") }.returns(true)
                coEvery { helperMock.isManagedUser("userId2") }.returns(false)

                cut.getOrCreateUser("userId1").shouldBe(user1)
                cut.getOrCreateUser("userId2").shouldBe(user2)

                coVerify {
                    userRepositoryMock.save(user1)
                    userRepositoryMock.save(user2)
                }
            }
            it("should use existing room") {
                val user = MatrixUser("userId")

                coEvery { userRepositoryMock.findById(any<String>()) }.returns(user)
                cut.getOrCreateUser("userId").shouldBe(user)
                coVerify(exactly = 0) { userRepositoryMock.save(any()) }
            }
        }
    }
}