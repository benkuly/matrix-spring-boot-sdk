package net.folivo.matrix.restclient.config

import net.folivo.matrix.restclient.annotation.MatrixEvent
import net.folivo.matrix.restclient.annotation.MatrixMessageEventContent
import net.folivo.matrix.restclient.model.events.Event
import net.folivo.matrix.restclient.model.events.m.room.message.MessageEvent
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.AnnotationUtils

class MatrixClientConfiguration {

    private val logger = LoggerFactory.getLogger(MatrixClientConfiguration::class.java)

    val registeredEvents: MutableMap<String, Class<out Event<*>>> = mutableMapOf()
    val registeredMessageEventContent: MutableMap<String, Class<out MessageEvent.MessageEventContent>> = mutableMapOf()

    fun configure(init: MatrixClientConfiguration.() -> Unit): MatrixClientConfiguration {
        return this.apply(init)
    }

    fun registerMatrixEvents(vararg events: Class<out Event<*>>) {
        events.forEach {
            val eventType = AnnotationUtils.findAnnotation(it, MatrixEvent::class.java)
            if (eventType == null) {
                logger.warn("$it has no ${MatrixEvent::class} annotation")
                return
            }
            registeredEvents[eventType.type] = it
            logger.info("registered event type ${eventType.type}")
        }
    }


    fun registerMessageEventContents(vararg messageEventContents: Class<out MessageEvent.MessageEventContent>) {
        messageEventContents.forEach {
            val messageType = AnnotationUtils.findAnnotation(it, MatrixMessageEventContent::class.java)
            if (messageType == null) {
                logger.warn("$it has no ${MatrixMessageEventContent::class}")
                return
            }
            registeredMessageEventContent[messageType.type] = it
            logger.info("registered message event content type ${messageType.type}")
        }
    }
}