package net.folivo.matrix.bot.client

import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.bot.handler.MatrixMessageEventHandler
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory
import reactor.core.Disposable

class MatrixClientBot(
        private val matrixClient: MatrixClient,
        private val eventHandler: List<MatrixEventHandler>,
        private val botProperties: MatrixBotProperties
) {

    private val logger = LoggerFactory.getLogger(MatrixMessageEventHandler::class.java)

    private var disposable: Disposable? = null

    fun start() {
        stop() // TODO or an exception?
        disposable = matrixClient.syncApi
                .syncLoop()
                .subscribe { syncResponse -> // TODO logic could be separated in something like a SyncResponseHandler
                    syncResponse.room.join.forEach { (roomId, joinedRoom) ->
                        joinedRoom.timeline.events.forEach { handleEvent(it, roomId) }
                        joinedRoom.state.events.forEach { handleEvent(it, roomId) }
                    }
                    if (botProperties.autoJoin) {
                        syncResponse.room.invite.keys.forEach { roomId ->
                            matrixClient.roomsApi.joinRoom(roomId).doOnSuccess {
                                logger.info("joined invitation to roomId: $it")
                            }.doOnError {
                                logger.error("could not join invitation to roomId: $roomId due to ${it.message}")
                            }.subscribe()
                        }
                    }
                }
    }

    fun stop() {
        disposable?.dispose()
    }

    private fun handleEvent(event: Event<*>, roomId: String) {
        eventHandler
                .filter { it.supports(event::class.java) }
                .forEach { it.handleEvent(event, roomId) }
    }
}