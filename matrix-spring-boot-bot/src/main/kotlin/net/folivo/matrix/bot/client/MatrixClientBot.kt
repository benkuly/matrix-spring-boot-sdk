package net.folivo.matrix.bot.client

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.folivo.matrix.bot.event.MatrixEventHandler
import net.folivo.matrix.bot.membership.MembershipChangeHandler
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.m.room.MemberEvent.MemberEventContent.Membership.JOIN
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

    fun start(): Job {
        stop()
        LOG.info("started syncLoop")
        val job = GlobalScope.launch {
            matrixClient.syncApi
                    .syncLoop()
                    .collect { syncResponse ->
                        try {
                            syncResponse.room.join.forEach { (roomId, joinedRoom) ->
                                joinedRoom.timeline.events.forEach { handleEvent(it, roomId) }
                                joinedRoom.state.events.forEach { handleEvent(it, roomId) }
                            }
                            syncResponse.room.invite.forEach { (roomId) ->//FIXME test
                                membershipChangeHandler.handleMembership(
                                        roomId, helper.getBotUserId(),
                                        JOIN
                                )
                            }
                            syncResponse.room.leave.forEach { (roomId) ->//FIXME test
                                membershipChangeHandler.handleMembership(
                                        roomId, helper.getBotUserId(),
                                        LEAVE
                                )
                            }
                            LOG.debug("processed sync response")
                        } catch (error: Throwable) {
                            LOG.error("some error while processing response", error.message)
                        }
                    }
        }
        syncJob = job
        return job
    }

    fun stop() {
        syncJob?.apply {
            LOG.info("stopped syncLoop")
            cancel()
        }
    }

    private suspend fun handleEvent(event: Event<*>, roomId: String) {
        return eventHandler
                .filter { it.supports(event::class.java) }
                .forEach { it.handleEvent(event, roomId) }
    }
}