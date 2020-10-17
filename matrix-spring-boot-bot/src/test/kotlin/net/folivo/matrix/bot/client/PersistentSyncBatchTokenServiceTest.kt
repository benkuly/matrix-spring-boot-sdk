package net.folivo.matrix.bot.client

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.*
import net.folivo.matrix.bot.util.BotServiceHelper

class PersistentSyncBatchTokenServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val syncBatchTokenRepositoryMock: MatrixSyncBatchTokenRepository = mockk(relaxed = true)
        val helperMock: BotServiceHelper = mockk {
            every { getBotUserId() }.returns("@bot:server")
        }

        val cut = PersistentSyncBatchTokenService(syncBatchTokenRepositoryMock, helperMock)

        describe(PersistentSyncBatchTokenService::getBatchToken.name) {
            it("should get token from repository") {
                coEvery { syncBatchTokenRepositoryMock.findByUserId("@user:server") }
                        .returns(MatrixSyncBatchToken("@user:server", "someToken"))
                cut.getBatchToken("@user:server").shouldBe("someToken")
            }
            it("should use bot user when no user given") {
                coEvery { syncBatchTokenRepositoryMock.findByUserId("@bot:server") }
                        .returns(MatrixSyncBatchToken("@user:server", "someToken"))
                cut.getBatchToken().shouldBe("someToken")
            }
            it("should return null when no token found") {
                coEvery { syncBatchTokenRepositoryMock.findByUserId("@user:server") }
                        .returns(MatrixSyncBatchToken("@user:server", null))
                cut.getBatchToken("@user:server").shouldBeNull()

                coEvery { syncBatchTokenRepositoryMock.findByUserId("@user:server") }
                        .returns(null)
                cut.getBatchToken("@user:server").shouldBeNull()
            }
        }

        describe(PersistentSyncBatchTokenService::setBatchToken.name) {
            describe("token does not exists in database") {
                beforeTest { coEvery { syncBatchTokenRepositoryMock.findByUserId(any()) }.returns(null) }
                it("should save new token") {
                    cut.setBatchToken("someToken", "@user:server")
                    coVerify { syncBatchTokenRepositoryMock.save(MatrixSyncBatchToken("@user:server", "someToken")) }
                }
                it("should use bot user when no user given") {
                    cut.setBatchToken("someToken")
                    coVerify { syncBatchTokenRepositoryMock.save(MatrixSyncBatchToken("@bot:server", "someToken")) }
                }
            }
            describe("token exists in database") {
                it("should override token") {
                    coEvery { syncBatchTokenRepositoryMock.findByUserId(any()) }
                            .returns(MatrixSyncBatchToken("@user:server", "someToken", version = 3))
                    cut.setBatchToken("someNewToken", "@user:server")
                    coVerify {
                        syncBatchTokenRepositoryMock
                                .save(MatrixSyncBatchToken("@user:server", "someNewToken", version = 3))
                    }
                }
                it("should use bot user when no user given") {
                    coEvery { syncBatchTokenRepositoryMock.findByUserId(any()) }
                            .returns(MatrixSyncBatchToken("@bot:server", "someToken", version = 3))
                    cut.setBatchToken("someNewToken")
                    coVerify {
                        syncBatchTokenRepositoryMock
                                .save(MatrixSyncBatchToken("@bot:server", "someNewToken", version = 3))
                    }
                }
            }
        }

        afterTest { clearMocks(syncBatchTokenRepositoryMock) }
    }
}