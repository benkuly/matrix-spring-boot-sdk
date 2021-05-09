package net.folivo.spring.matrix.bot.event

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.*
import net.folivo.spring.matrix.bot.config.MatrixBotProperties
import net.folivo.spring.matrix.bot.user.MatrixUserService
import net.folivo.trixnity.core.model.MatrixId

class PersistentSyncBatchTokenServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val syncBatchTokenRepositoryMock: MatrixSyncBatchTokenRepository = mockk(relaxed = true)

        val botUserId = MatrixId.UserId("bot", "server")
        val userId = MatrixId.UserId("user", "server")

        val botPropertiesMock: MatrixBotProperties = mockk()
        val userServiceMock: MatrixUserService = mockk(relaxed = true)

        val cut = PersistentSyncBatchTokenService(syncBatchTokenRepositoryMock, userServiceMock, botPropertiesMock)

        beforeTest {
            every { botPropertiesMock.botUserId }.returns(botUserId)
        }

        describe(PersistentSyncBatchTokenService::getBatchToken.name) {
            it("should get token from repository") {
                coEvery { syncBatchTokenRepositoryMock.findByUserId(userId) }
                    .returns(MatrixSyncBatchToken(userId, "someToken"))
                cut.getBatchToken(userId).shouldBe("someToken")
            }
            it("should use bot user when no user given") {
                coEvery { syncBatchTokenRepositoryMock.findByUserId(botUserId) }
                    .returns(MatrixSyncBatchToken(userId, "someToken"))
                cut.getBatchToken().shouldBe("someToken")
            }
            it("should return null when no token found") {
                coEvery { syncBatchTokenRepositoryMock.findByUserId(userId) }
                    .returns(MatrixSyncBatchToken(userId, null))
                cut.getBatchToken(userId).shouldBeNull()

                coEvery { syncBatchTokenRepositoryMock.findByUserId(userId) }
                    .returns(null)
                cut.getBatchToken(userId).shouldBeNull()
            }
        }

        describe(PersistentSyncBatchTokenService::setBatchToken.name) {
            describe("token does not exists in database") {
                beforeTest { coEvery { syncBatchTokenRepositoryMock.findByUserId(any()) }.returns(null) }
                it("should save new token") {
                    cut.setBatchToken("someToken", userId)
                    coVerify { userServiceMock.getOrCreateUser(userId) }
                    coVerify { syncBatchTokenRepositoryMock.save(MatrixSyncBatchToken(userId, "someToken")) }
                }
                it("should use bot user when no user given") {
                    cut.setBatchToken("someToken")
                    coVerify { userServiceMock.getOrCreateUser(botUserId) }
                    coVerify { syncBatchTokenRepositoryMock.save(MatrixSyncBatchToken(botUserId, "someToken")) }
                }
            }
            describe("token exists in database") {
                it("should override token") {
                    coEvery { syncBatchTokenRepositoryMock.findByUserId(any()) }
                        .returns(MatrixSyncBatchToken(userId, "someToken", version = 3))
                    cut.setBatchToken("someNewToken", userId)
                    coVerify {
                        syncBatchTokenRepositoryMock
                            .save(MatrixSyncBatchToken(userId, "someNewToken", version = 3))
                    }
                }
                it("should use bot user when no user given") {
                    coEvery { syncBatchTokenRepositoryMock.findByUserId(any()) }
                        .returns(MatrixSyncBatchToken(botUserId, "someToken", version = 3))
                    cut.setBatchToken("someNewToken")
                    coVerify {
                        syncBatchTokenRepositoryMock
                            .save(MatrixSyncBatchToken(botUserId, "someNewToken", version = 3))
                    }
                }
            }
        }

        afterTest { clearMocks(syncBatchTokenRepositoryMock, userServiceMock) }
    }
}