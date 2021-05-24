package net.folivo.spring.matrix.bot.event

import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.m.room.MessageEventContent
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class MessageEventHandler(
    private val messageHandler: List<MatrixMessageHandler>,
    private val matrixClient: MatrixClient
) : MatrixEventHandler<MessageEventContent> {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override fun supports(): KClass<MessageEventContent> {
        return MessageEventContent::class
    }

    override suspend fun handleEvent(event: Event<out MessageEventContent>) {
        require(event is Event.RoomEvent)

        val messageContext = MessageContext(
            matrixClient,
            event,
            event.roomId
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