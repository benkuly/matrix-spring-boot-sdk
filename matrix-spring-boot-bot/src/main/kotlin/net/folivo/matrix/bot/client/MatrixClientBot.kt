package net.folivo.matrix.bot.client

import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import reactor.core.Disposable
import reactor.core.publisher.Flux

class MatrixClientBot(
        private val matrixClient: MatrixClient,
        private val eventHandler: List<MatrixEventHandler>,
        private val botProperties: MatrixBotProperties
) {

    private val logger = LoggerFactory.getLogger(MatrixClientBot::class.java)

    private var disposable: Disposable? = null

    fun start() {
        stop() // TODO or an exception?
        logger.info("started syncLoop")
        disposable = matrixClient.syncApi // FIXME make reactive
                .syncLoop()
                .doOnError { logger.error("error in syncLoop", it) }
                .subscribe { syncResponse -> // TODO logic could be separated in something like a SyncResponseHandler, also flatMap would be cool
                    syncResponse.room.join.forEach { (roomId, joinedRoom) ->
                        joinedRoom.timeline.events.forEach { handleEvent(it, roomId).subscribe() }
                        joinedRoom.state.events.forEach { handleEvent(it, roomId).subscribe() }
                    }
                    if (botProperties.autoJoin == MatrixBotProperties.AutoJoinMode.DISABLED) {
                        syncResponse.room.invite.keys.forEach { roomId ->
                            if (botProperties.autoJoin == MatrixBotProperties.AutoJoinMode.RESTRICTED
                                && roomId.substringAfter(":") != botProperties.serverName
                            ) {
                                logger.warn("reject room invite to $roomId because autoJoin is restricted to ${botProperties.serverName}")
                                matrixClient.roomsApi.leaveRoom(roomId)
                            } else {
                                matrixClient.roomsApi.joinRoom(roomId).doOnSuccess {
                                    logger.info("joined invitation to roomId: $it")
                                }.doOnError {
                                    logger.error("could not join invitation to roomId: $roomId due to ${it.message}")
                                }.subscribe()
                            }
                        }
                    }
                }
    }

    fun stop() {
        logger.info("stopped syncLoop")
        disposable?.dispose()
    }

    private fun handleEvent(event: Event<*>, roomId: String): Flux<Void> {
        return Flux.fromIterable(eventHandler)
                .filter { it.supports(event::class.java) }
                .flatMap { it.handleEvent(event, roomId) }
    }
}