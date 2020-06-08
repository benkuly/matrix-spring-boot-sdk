package net.folivo.matrix.bot.handler

import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

class MessageContext(
        val matrixClient: MatrixClient,
        val originalEvent: MessageEvent<*>,
        val roomId: String
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    fun answer(
            content: MessageEvent.MessageEventContent,
            asUserId: String? = null
    ): Mono<String> {
        return matrixClient.roomsApi.sendRoomEvent(
                roomId = roomId,
                eventContent = content,
                asUserId = asUserId
        ).retryWhen(Retry.backoff(3, Duration.ofMillis(500)))
                .doOnError { LOG.warn("could not answer to $roomId", it) }
                .onErrorResume { Mono.empty() }
    }
}