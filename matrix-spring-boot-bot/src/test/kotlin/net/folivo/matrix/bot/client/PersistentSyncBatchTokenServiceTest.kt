package net.folivo.matrix.bot.client

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.*
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.core.model.MatrixId.UserId

class PersistentSyncBatchTokenServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val syncBatchTokenRepositoryMock: MatrixSyncBatchTokenRepository = mockk(relaxed = true)

        val botUserId = UserId("bot", "server")
        val userId = UserId("user", "server")

        val helperMock: BotServiceHelper = mockk {
            every { getBotUserId() }.returns(botUserId)
        }

        val cut = PersistentSyncBatchTokenService(syncBatchTokenRepositoryMock, helperMock)

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
                    coVerify { syncBatchTokenRepositoryMock.save(MatrixSyncBatchToken(userId, "someToken")) }
                }
                it("should use bot user when no user given") {
                    cut.setBatchToken("someToken")
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

        afterTest { clearMocks(syncBatchTokenRepositoryMock) }
    }
}