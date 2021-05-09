package net.folivo.spring.matrix.bot.client

import kotlinx.coroutines.runBlocking
import net.folivo.trixnity.client.rest.MatrixClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

class MatrixClientSyncRunner(
    private val matrixClient: MatrixClient,
) : DisposableBean {

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @EventListener(ApplicationReadyEvent::class)
    fun startClientJob() {
        runBlocking {
            LOG.debug("starting sync")
            matrixClient.sync.start()
        }
    }

    override fun destroy() {
        runBlocking {
            LOG.debug("stopping sync")
            matrixClient.sync.stop()
        }
    }
}