package net.folivo.matrix.bot.appservice.event

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.folivo.matrix.bot.config.MatrixBotDatabaseAutoconfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.from
import org.springframework.data.r2dbc.core.into
import org.springframework.data.relational.core.query.CriteriaDefinition

@DataR2dbcTest
@ImportAutoConfiguration(MatrixBotDatabaseAutoconfiguration::class)
class MatrixEventTransactionRepositoryTest(
        cut: MatrixEventTransactionRepository,
        dbClient: DatabaseClient
) : DescribeSpec(testBody(cut, dbClient))

private fun testBody(cut: MatrixEventTransactionRepository, dbClient: DatabaseClient): DescribeSpec.() -> Unit {
    return {
        beforeTest {
            println("beforeEach")
            dbClient.delete()
                    .from<MatrixEventTransaction>()
                    .matching(CriteriaDefinition.empty())
                    .then().awaitFirstOrNull()
        }

        describe(MatrixEventTransactionRepository::existsByTnxIdAndEventId.name) {
            it("when transaction exists in database it should return true") {
                println("insert")
                dbClient.insert()
                        .into<MatrixEventTransaction>()
                        .using(MatrixEventTransaction("someTnxId", "someIdOrHash"))
                        .then().awaitFirstOrNull()
                println("cut")
                cut.existsByTnxIdAndEventId("someTnxId", "someIdOrHash").shouldBeTrue()
                println("finish")
            }
            it("when transaction does not exists in database it should return false") {
                cut.existsByTnxIdAndEventId("someTnxId", "someIdOrHash").shouldBeFalse()
            }
        }
    }
}