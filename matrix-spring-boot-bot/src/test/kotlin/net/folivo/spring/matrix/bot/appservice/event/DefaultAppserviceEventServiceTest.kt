package net.folivo.spring.matrix.bot.appservice.event

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.trixnity.appservice.rest.event.AppserviceEventTnxService.EventTnxProcessingState.NOT_PROCESSED
import net.folivo.trixnity.appservice.rest.event.AppserviceEventTnxService.EventTnxProcessingState.PROCESSED

class DefaultAppserviceEventServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {

        val eventTransactionServiceMock = mockk<MatrixEventTransactionService>(relaxed = true)

        val cut = DefaultAppserviceEventTnxService(
            eventTransactionServiceMock,
        )

        describe(DefaultAppserviceEventTnxService::eventTnxProcessingState.name) {
            describe("event already processed") {
                it("should return $PROCESSED") {
                    coEvery { eventTransactionServiceMock.hasTransaction("someTnxId") }
                        .returns(true)
                    cut.eventTnxProcessingState("someTnxId")
                        .shouldBe(PROCESSED)
                }
            }
            describe("event not processed") {
                it("should return $NOT_PROCESSED") {
                    coEvery { eventTransactionServiceMock.hasTransaction("someTnxId") }
                        .returns(false)
                    cut.eventTnxProcessingState("someTnxId")
                        .shouldBe(NOT_PROCESSED)
                }
            }
        }
        describe(DefaultAppserviceEventTnxService::onEventTnxProcessed.name) {
            it("should save event as processed") {
                cut.onEventTnxProcessed("someTnxId")

                coVerify {
                    eventTransactionServiceMock.saveTransaction(MatrixEventTransaction("someTnxId"))
                }
            }
        }

        afterTest { clearMocks(eventTransactionServiceMock) }
    }
}