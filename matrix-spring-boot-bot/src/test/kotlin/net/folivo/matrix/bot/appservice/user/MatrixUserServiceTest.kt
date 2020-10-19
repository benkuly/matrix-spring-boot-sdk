package net.folivo.matrix.bot.appservice.user

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.core.model.MatrixId.UserId

class MatrixUserServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val userRepositoryMock: MatrixUserRepository = mockk(relaxed = true)
        val helperMock: BotServiceHelper = mockk()

        val cut = MatrixUserService(userRepositoryMock, helperMock)

        val userId1 = UserId("user1", "server")
        val userId2 = UserId("user2", "server")

        describe(MatrixUserService::getOrCreateUser.name) {

            it("should save new user when user does not exist") {
                val user1 = MatrixUser(userId1, true)
                val user2 = MatrixUser(userId2, false)

                coEvery { userRepositoryMock.findById(any()) }.returns(null)
                coEvery { userRepositoryMock.save(user1) }.returns(user1)
                coEvery { userRepositoryMock.save(user2) }.returns(user2)
                coEvery { helperMock.isManagedUser(userId1) }.returns(true)
                coEvery { helperMock.isManagedUser(userId2) }.returns(false)

                cut.getOrCreateUser(userId1).shouldBe(user1)
                cut.getOrCreateUser(userId2).shouldBe(user2)

                coVerify {
                    userRepositoryMock.save(user1)
                    userRepositoryMock.save(user2)
                }
            }
            it("should use existing room") {
                val user = MatrixUser(userId1)

                coEvery { userRepositoryMock.findById(any()) }.returns(user)
                cut.getOrCreateUser(userId1).shouldBe(user)
                coVerify(exactly = 0) { userRepositoryMock.save(any()) }
            }
        }

        afterTest { clearMocks(userRepositoryMock, helperMock) }
    }
}