package net.folivo.matrix.bot.handler

import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory

class MatrixMessageEventHandler(
        private val messageContentHandler: List<MatrixMessageContentHandler>,
        private val matrixClient: MatrixClient
) : MatrixEventHandler {

    private val logger = LoggerFactory.getLogger(MatrixMessageEventHandler::class.java)


    override fun supports(clazz: Class<*>): Boolean {
        return clazz == MessageEvent::class.java
    }

    override fun handleEvent(event: Event<*>, roomId: String?) {
        if (event is MessageEvent<*>) {
            if (roomId == null) {
                logger.info("could not handle event due to missing roomId")
                return
            }
            val messageContext = MessageContext(
                    matrixClient,
                    event,
                    roomId
            )
            logger.debug("handle message event")
            messageContentHandler.forEach { it.handleMessage(event.content, messageContext) }
        }
    }
}