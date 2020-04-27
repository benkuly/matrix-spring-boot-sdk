package net.folivo.matrix.bot.handler

import net.folivo.matrix.common.model.events.Event
import net.folivo.matrix.common.model.events.m.room.message.MessageEvent
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory

class MatrixMessageEventHandler(private val messageContentHandler: List<MatrixMessageContentHandler>) : MatrixEventHandler {

    private val logger = LoggerFactory.getLogger(MatrixMessageEventHandler::class.java)


    override fun supports(clazz: Class<*>): Boolean {
        return clazz == MessageEvent::class.java
    }

    override fun handleEvent(event: Event<*>, roomId: String, matrixClient: MatrixClient) {
        if (event is MessageEvent<*>) {
            logger.debug("handle message event")
            val messageContext = MessageContext(
                    matrixClient,
                    event,
                    roomId
            )
            messageContentHandler.forEach { it.handleMessage(event.content, messageContext) }
        }
    }
}