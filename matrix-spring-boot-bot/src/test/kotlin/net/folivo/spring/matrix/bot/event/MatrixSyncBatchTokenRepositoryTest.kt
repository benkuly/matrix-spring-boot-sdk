package net.folivo.spring.matrix.bot.event

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.folivo.spring.matrix.bot.config.MatrixBotDatabaseAutoconfiguration
import net.folivo.spring.matrix.bot.user.MatrixUser
import net.folivo.trixnity.core.model.MatrixId
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.delete

@DataR2dbcTest
@ImportAutoConfiguration(MatrixBotDatabaseAutoconfiguration::class)
class MatrixSyncBatchTokenRepositoryTest(
    cut: MatrixSyncBatchTokenRepository,
    db: R2dbcEntityTemplate
) : DescribeSpec(testBody(cut, db))

private fun testBody(cut: MatrixSyncBatchTokenRepository, db: R2dbcEntityTemplate): DescribeSpec.() -> Unit {
    return {
        val userId = MatrixId.UserId("user", "server")
        beforeSpec {
            db.insert(MatrixUser(userId, true)).awaitFirstOrNull()
            db.insert(MatrixSyncBatchToken(userId, "someToken")).awaitFirstOrNull()
        }

        describe(MatrixSyncBatchTokenRepository::findByUserId.name) {
            it("should find matching token") {
                cut.findByUserId(userId).shouldBe(MatrixSyncBatchToken(userId, "someToken", 1))
            }
            it("should not find matching token") {
                cut.findByUserId(MatrixId.UserId("unknown", "server")).shouldBeNull()
            }
        }

        afterSpec {
            db.delete<MatrixSyncBatchToken>().all().awaitFirstOrNull()
            db.delete<MatrixUser>().all().awaitFirstOrNull()
        }
    }
}