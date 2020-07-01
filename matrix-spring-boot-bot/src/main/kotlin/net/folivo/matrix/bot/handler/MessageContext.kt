package net.folivo.matrix.bot.handler

import com.github.michaelbull.retry.policy.binaryExponentialBackoff
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory

class MessageContext(
        val matrixClient: MatrixClient,
        val originalEvent: MessageEvent<*>,
        val roomId: String
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun answer(
            content: MessageEvent.MessageEventContent,
            asUserId: String? = null
    ): String {
        return try {
            retry(limitAttempts(3) + binaryExponentialBackoff(LongRange(500, 5000))) {
                matrixClient.roomsApi.sendRoomEvent(
                        roomId = roomId,
                        eventContent = content,
                        asUserId = asUserId
                )
            }
        } catch (error: Throwable) {
            LOG.warn("could not answer to $roomId", error)
            throw error
        }
    }
}