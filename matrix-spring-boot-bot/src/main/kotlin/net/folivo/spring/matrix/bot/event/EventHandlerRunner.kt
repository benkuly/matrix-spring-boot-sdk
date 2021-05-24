package net.folivo.spring.matrix.bot.event

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import net.folivo.trixnity.core.EventEmitter
import net.folivo.trixnity.core.model.events.Event
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

class EventHandlerRunner(
    private val eventEmitter: EventEmitter,
    private val eventHandler: List<MatrixEventHandler<*>>,
) : DisposableBean {
    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(this::class.java)
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private val jobs = mutableListOf<Job>()

    @EventListener(ApplicationReadyEvent::class)
    fun startEventListening() {
        runBlocking {
            LOG.debug("started event listening")
            eventHandler.forEach { handler ->
                val eventFlow = eventEmitter.events(handler.supports())
                jobs.add(scope.launch {
                    @Suppress("UNCHECKED_CAST")
                    eventFlow.collect { handler.handleEvent(it as Event<Nothing>) }
                })
            }
        }
    }

    override fun destroy() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }
}