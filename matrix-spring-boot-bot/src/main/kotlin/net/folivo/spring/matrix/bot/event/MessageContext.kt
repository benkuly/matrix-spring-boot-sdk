package net.folivo.spring.matrix.bot.event

import com.github.michaelbull.retry.policy.binaryExponentialBackoff
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.core.model.MatrixId
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.m.room.MessageEventContent
import org.slf4j.LoggerFactory

class MessageContext(
    val matrixClient: MatrixClient,
    val originalEvent: Event.RoomEvent<out MessageEventContent>,
    val roomId: MatrixId.RoomId
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun answer(
        content: MessageEventContent,
        asUserId: MatrixId.UserId? = null
    ): MatrixId.EventId {
        return try {
            retry(limitAttempts(5) + binaryExponentialBackoff(LongRange(500, 10000))) {
                matrixClient.room.sendRoomEvent(
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
        asUserId: MatrixId.UserId? = null
    ): MatrixId.EventId {
        return answer(MessageEventContent.NoticeMessageEventContent(content), asUserId)
    }
}