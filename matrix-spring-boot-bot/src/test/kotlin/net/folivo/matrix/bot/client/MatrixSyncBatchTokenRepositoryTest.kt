package net.folivo.matrix.bot.client

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.folivo.matrix.bot.RepositoryTestHelper
import net.folivo.matrix.bot.appservice.user.MatrixUser
import net.folivo.matrix.bot.config.MatrixBotDatabaseAutoconfiguration
import net.folivo.matrix.core.model.MatrixId.UserId
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.from
import org.springframework.data.r2dbc.core.into
import org.springframework.data.relational.core.query.CriteriaDefinition

@DataR2dbcTest
@ImportAutoConfiguration(MatrixBotDatabaseAutoconfiguration::class)
class MatrixSyncBatchTokenRepositoryTest(
        cut: MatrixSyncBatchTokenRepository,
        dbClient: DatabaseClient
) : DescribeSpec(testBody(cut, dbClient))

private fun testBody(cut: MatrixSyncBatchTokenRepository, dbClient: DatabaseClient): DescribeSpec.() -> Unit {
    return {
        val h = RepositoryTestHelper(dbClient)

        val userId = UserId("user", "server")
        beforeSpec {
            h.insertUser(MatrixUser(userId, true))
            dbClient.insert()
                    .into<MatrixSyncBatchToken>()
                    .using(MatrixSyncBatchToken(userId, "someToken"))
                    .then().awaitFirstOrNull()
        }

        describe(MatrixSyncBatchTokenRepository::findByUserId.name) {
            it("should find matching token") {
                cut.findByUserId(userId).shouldBe(MatrixSyncBatchToken(userId, "someToken"))
            }
            it("should not find matching token") {
                cut.findByUserId(UserId("unknown", "server")).shouldBeNull()
            }
        }

        afterSpec {
            dbClient.delete()
                    .from<MatrixSyncBatchToken>()
                    .matching(CriteriaDefinition.empty())
                    .then().awaitFirstOrNull()
            h.deleteAllUsers()
        }
    }
}