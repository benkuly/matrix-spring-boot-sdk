package net.folivo.matrix.bot.appservice.event

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.from
import org.springframework.data.r2dbc.core.into
import org.springframework.data.relational.core.query.CriteriaDefinition

@DataR2dbcTest
class MatrixEventTransactionRepositoryTest(
        cut: MatrixEventTransactionRepository,
        dbClient: DatabaseClient
) : DescribeSpec(testBody(cut, dbClient))

private fun testBody(cut: MatrixEventTransactionRepository, dbClient: DatabaseClient): DescribeSpec.() -> Unit {
    return {
        beforeEach {//FIXME does this work?
            dbClient.delete().from<MatrixEventTransaction>().matching(CriteriaDefinition.empty()).then().awaitFirst()
        }

        describe(MatrixEventTransactionRepository::containsByTnxIdAndEventIdOrHash.name) {
            describe("when transaction exists in database") {
                dbClient.insert()
                        .into<MatrixEventTransaction>()
                        .using(MatrixEventTransaction("someTnxId", "someIdOrHash"))
                        .then().awaitFirst()
                it("should return true") {
                    cut.containsByTnxIdAndEventIdOrHash("someTnxId", "someIdOrHash").awaitFirstOrNull().shouldBe(true)
                }
            }
            describe("when transaction does not exists in database") {
                it("should return false") {
                    cut.containsByTnxIdAndEventIdOrHash("unknown", "unknown").awaitFirstOrNull().shouldBe(false)
                }
            }
        }
    }
}