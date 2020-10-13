package net.folivo.matrix.bot.appservice.user

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.folivo.matrix.bot.util.BotServiceHelper
import reactor.core.publisher.Mono

class MatrixUserServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val userRepositoryMock: MatrixUserRepository = mockk(relaxed = true)
        val helperMock: BotServiceHelper = mockk(relaxed = true)

        val cut = MatrixUserService(userRepositoryMock, helperMock)

        describe(MatrixUserService::getOrCreateUser.name) {
            val user = MatrixUser("userId")

            it("should save new user when user does not exist") {
                every { userRepositoryMock.findById(any<String>()) }.returns(Mono.empty())
                every { userRepositoryMock.save(any()) }.returns(Mono.just(user))
                cut.getOrCreateUser("userId").shouldBe(user)
                verify { userRepositoryMock.save(user) }
            }
            it("should use existing room") {
                every { userRepositoryMock.findById(any<String>()) }.returns(Mono.just(user))
                cut.getOrCreateUser("userId").shouldBe(user)
                verify(exactly = 0) { userRepositoryMock.save(any()) }
            }
        }
    }
}