package net.folivo.matrix.bot.event

import com.github.michaelbull.retry.policy.binaryExponentialBackoff
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import net.folivo.matrix.core.model.MatrixId.*
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.NoticeMessageEventContent
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory

class MessageContext(
        val matrixClient: MatrixClient,
        val originalEvent: MessageEvent<*>,
        val roomId: RoomId
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun answer(
            content: MessageEvent.MessageEventContent,
            asUserId: UserId? = null
    ): EventId {
        return try {
            retry(limitAttempts(5) + binaryExponentialBackoff(LongRange(500, 10000))) {
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

    suspend fun answer(
            content: String,
            asUserId: UserId? = null
    ): EventId {
        return answer(NoticeMessageEventContent(content), asUserId)
    }
}