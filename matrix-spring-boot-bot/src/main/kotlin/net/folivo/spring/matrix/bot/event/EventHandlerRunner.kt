package net.folivo.spring.matrix.bot.event

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.folivo.trixnity.core.EventEmitter
import net.folivo.trixnity.core.model.events.Event
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

class EventHandlerRunner(
    private val eventEmitter: EventEmitter,
    private val eventHandler: List<MatrixEventHandler<*>>,
) {
    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @EventListener(ApplicationReadyEvent::class)
    fun startEventListening() {
        runBlocking {
            LOG.debug("started event listening")
            eventHandler.forEach { handler ->
                val eventFlow = eventEmitter.events(handler.supports())
                launch(Dispatchers.Default) {
                    eventFlow.collect { handler.handleEvent(it as Event<Nothing>) }
                }
            }
        }
    }
}