package net.folivo.spring.matrix.appservice

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import net.folivo.trixnity.appservice.rest.AppserviceService
import net.folivo.trixnity.appservice.rest.matrixAppserviceModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import kotlin.concurrent.thread

class AppserviceApplicationEngine(
    properties: MatrixAppserviceConfigurationProperties,
    appserviceService: AppserviceService
) : DisposableBean {

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(this::class.java)
    }

    private val engine: ApplicationEngine = embeddedServer(CIO, port = properties.port) {
        matrixAppserviceModule(properties.toMatrixAppserviceProperties(), appserviceService)
    }

    @EventListener(ApplicationReadyEvent::class)
    fun startApplicationEngine() {
        thread {
            LOG.debug("starting appservice webserver")
            engine.start(wait = true)
        }
    }

    override fun destroy() {
        engine.stop(5000, 500)
    }
}