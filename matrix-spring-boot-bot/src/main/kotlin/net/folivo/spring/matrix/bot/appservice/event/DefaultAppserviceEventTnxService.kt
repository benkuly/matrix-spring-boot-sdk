package net.folivo.spring.matrix.bot.appservice.event

import net.folivo.trixnity.appservice.rest.event.AppserviceEventTnxService
import net.folivo.trixnity.appservice.rest.event.AppserviceEventTnxService.EventTnxProcessingState

open class DefaultAppserviceEventTnxService(
    private val eventTransactionService: MatrixEventTransactionService,
) : AppserviceEventTnxService {

    override suspend fun eventTnxProcessingState(tnxId: String): EventTnxProcessingState {
        return if (eventTransactionService.hasTransaction(tnxId))
            EventTnxProcessingState.PROCESSED
        else
            EventTnxProcessingState.NOT_PROCESSED
    }

    override suspend fun onEventTnxProcessed(tnxId: String) {
        eventTransactionService.saveTransaction(MatrixEventTransaction(tnxId))
    }
}