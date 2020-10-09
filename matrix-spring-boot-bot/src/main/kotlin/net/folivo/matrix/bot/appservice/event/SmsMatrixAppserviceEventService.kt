package net.folivo.matrix.bot.appservice.event

import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.folivo.matrix.appservice.api.event.AppserviceEventService.EventProcessingState
import net.folivo.matrix.bot.appservice.DefaultAppserviceEventService
import net.folivo.matrix.bot.event.MatrixEventHandler
import org.springframework.stereotype.Service

@Service
class SmsMatrixAppserviceEventService(
        private val eventTransactionRepository: MatrixEventTransactionRepository,
        eventHandler: List<MatrixEventHandler>
) : DefaultAppserviceEventService(eventHandler) {

    override suspend fun eventProcessingState(
            tnxId: String,
            eventIdOrType: String
    ): EventProcessingState {
        return if (eventTransactionRepository.findByTnxIdAndEventIdElseType(tnxId, eventIdOrType)
                        .awaitFirstOrNull() != null)
            EventProcessingState.PROCESSED
        else
            EventProcessingState.NOT_PROCESSED
    }

    override suspend fun onEventProcessed(tnxId: String, eventIdOrType: String) {
        eventTransactionRepository.save(MatrixEventTransaction(tnxId, eventIdOrType)).awaitFirstOrNull()
    }
}