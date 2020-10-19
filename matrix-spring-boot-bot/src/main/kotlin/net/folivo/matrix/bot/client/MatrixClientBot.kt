package net.folivo.matrix.bot.client

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.bot.membership.MembershipChangeHandler
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.INVITE
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.LEAVE
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

class MatrixClientBot(
        private val matrixClient: MatrixClient,
        private val eventHandler: List<MatrixEventHandler>,
        private val membershipChangeHandler: MembershipChangeHandler,
        private val helper: BotServiceHelper
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    private var syncJob: Job? = null

    @EventListener(ApplicationReadyEvent::class)
    fun startClientJob() {
        runBlocking { start().join() }
    }

    suspend fun start(): Job {
        stop()
        LOG.info("started syncLoop")
        val job = GlobalScope.launch {
            try {
                matrixClient.syncApi
                        .syncLoop()
                        .collect { syncResponse ->
                            try {
                                syncResponse.room.join.forEach { (roomId, joinedRoom) ->
                                    joinedRoom.timeline.events.forEach { handleEvent(it, roomId) }
                                    joinedRoom.state.events.forEach { handleEvent(it, roomId) }
                                }
                                syncResponse.room.invite.forEach { (roomId) ->
                                    membershipChangeHandler.handleMembership(helper.getBotUserId(), roomId, INVITE)
                                }
                                syncResponse.room.leave.forEach { (roomId) ->
                                    membershipChangeHandler.handleMembership(helper.getBotUserId(), roomId, LEAVE)
                                }
                                LOG.debug("processed sync response")
                            } catch (error: Throwable) {
                                LOG.error("some error while processing response", error.message)
                            }
                        }
            } catch (error: CancellationException) {
                LOG.info("stopped syncLoop")
            }
        }
        syncJob = job
        return job
    }

    suspend fun stop() {
        syncJob?.cancelAndJoin()
    }

    private suspend fun handleEvent(event: Event<*>, roomId: RoomId) {
        return eventHandler.asFlow()
                .filter { it.supports(event::class.java) }
                .collect { it.handleEvent(event, roomId) }
    }
}