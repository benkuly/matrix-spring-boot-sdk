package net.folivo.matrix.bot.appservice.event

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.folivo.matrix.bot.config.MatrixBotDatabaseAutoconfiguration
import net.folivo.matrix.core.model.MatrixId.EventId
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.delete

@DataR2dbcTest
@ImportAutoConfiguration(MatrixBotDatabaseAutoconfiguration::class)
class MatrixEventTransactionRepositoryTest(
        cut: MatrixEventTransactionRepository,
        db: R2dbcEntityTemplate
) : DescribeSpec(testBody(cut, db))

private fun testBody(cut: MatrixEventTransactionRepository, db: R2dbcEntityTemplate): DescribeSpec.() -> Unit {
    return {

        beforeSpec {
            db.insert(MatrixEventTransaction("someTnxId", EventId("event", "server"))).awaitFirstOrNull()
        }

        describe(MatrixEventTransactionRepository::existsByTnxIdAndEventId.name) {
            it("when transaction exists in database it should return true") {
                cut.existsByTnxIdAndEventId("someTnxId", EventId("event", "server")).shouldBeTrue()
            }
            it("when transaction does not exists in database it should return false") {
                cut.existsByTnxIdAndEventId("someUnknownTnxId", EventId("event", "server")).shouldBeFalse()
            }
        }

        afterSpec {
            db.delete<MatrixEventTransaction>().all().awaitFirstOrNull()
        }
    }
}