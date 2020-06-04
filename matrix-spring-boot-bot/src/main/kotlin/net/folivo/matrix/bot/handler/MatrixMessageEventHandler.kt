package net.folivo.matrix.bot.handler

import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class MatrixMessageEventHandler(
        private val messageContentHandler: List<MatrixMessageContentHandler>,
        private val matrixClient: MatrixClient
) : MatrixEventHandler {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override fun supports(clazz: Class<*>): Boolean {
        return clazz == MessageEvent::class.java
    }

    override fun handleEvent(event: Event<*>, roomId: String?): Mono<Void> {
        if (event is MessageEvent<*>) {
            if (roomId == null) {
                LOG.info("could not handle event due to missing roomId")
                return Mono.empty()
            }
            val messageContext = MessageContext(
                    matrixClient,
                    event,
                    roomId
            )
            LOG.debug("handle message event")
            return Flux.fromIterable(messageContentHandler)
                    .flatMap { it.handleMessage(event.content, messageContext) }
                    .onErrorContinue { throwable, _ -> LOG.warn("could not handle message due to $throwable") }
                    .then()
        }
        return Mono.empty()
    }
}