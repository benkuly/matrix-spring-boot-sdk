package net.folivo.matrix.bot.client

import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.config.MatrixBotProperties.AutoJoinMode.DISABLED
import net.folivo.matrix.bot.handler.AutoJoinService
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class MatrixClientBot(
        private val matrixClient: MatrixClient,
        private val eventHandler: List<MatrixEventHandler>,
        private val botProperties: MatrixBotProperties,
        private val autoJoinService: AutoJoinService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    private var disposable: Disposable? = null

    fun start() {
        stop()
        LOG.info("started syncLoop")
        disposable = matrixClient.syncApi
                .syncLoop()
                .flatMap { syncResponse ->
                    val actions = mutableListOf<Mono<Void>>()
                    syncResponse.room.join.forEach { (roomId, joinedRoom) ->
                        joinedRoom.timeline.events.forEach { actions.add(handleEvent(it, roomId)) }
                        joinedRoom.state.events.forEach { actions.add(handleEvent(it, roomId)) }
                    }
                    syncResponse.room.invite.keys.forEach { roomId ->
                        if (botProperties.autoJoin == DISABLED
                            || botProperties.autoJoin == MatrixBotProperties.AutoJoinMode.RESTRICTED
                            && roomId.substringAfter(":") != botProperties.serverName
                        ) {
                            LOG.warn("reject room invite to $roomId because autoJoin is not allowed to ${botProperties.serverName}")
                            actions.add(matrixClient.roomsApi.leaveRoom(roomId))
                        } else {
                            actions.add(autoJoinService.shouldJoin(roomId, botProperties.username)
                                                .flatMap {
                                                    if (it) {
                                                        LOG.debug("join invitation to roomId: $roomId")
                                                        matrixClient.roomsApi.joinRoom(roomId).then()
                                                    } else {
                                                        LOG.debug("reject room invite to $roomId because service denied it")
                                                        matrixClient.roomsApi.leaveRoom(roomId)
                                                    }
                                                })
                        }
                    }
                    Flux.merge(actions).then()
                }
                .subscribe(
                        { LOG.debug("processed sync response") },
                        { LOG.error("some error while processing response", it) }
                )
    }

    fun stop() {
        disposable?.apply {
            LOG.info("stopped syncLoop")
            dispose()
        }
    }

    private fun handleEvent(event: Event<*>, roomId: String): Mono<Void> {
        return Flux.fromIterable(eventHandler)
                .filter { it.supports(event::class.java) }
                .flatMap { it.handleEvent(event, roomId) }
                .then()
    }
}