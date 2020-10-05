package net.folivo.matrix.bot.handler

import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory

class MatrixMessageEventHandler(
        private val messageHandler: List<MatrixMessageHandler>,
        private val matrixClient: MatrixClient
) : MatrixEventHandler {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override fun supports(clazz: Class<*>): Boolean {
        return clazz == MessageEvent::class.java
    }

    override suspend fun handleEvent(event: Event<*>, roomId: String?) {
        if (event is MessageEvent<*>) {
            if (roomId == null) {
                LOG.info("could not handle event due to missing roomId")
                return
            }
            val messageContext = MessageContext(
                    matrixClient,
                    event,
                    roomId
            )
            LOG.debug("handle message event")
            messageHandler
                    .forEach {
                        try {
                            it.handleMessage(event.content, messageContext)
                        } catch (error: Throwable) {
                            LOG.warn("could not handle message", error)
                        }
                    }
        }
    }
}