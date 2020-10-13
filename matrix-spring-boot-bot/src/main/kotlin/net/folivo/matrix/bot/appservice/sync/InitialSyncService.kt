package net.folivo.matrix.bot.appservice.sync

import kotlinx.coroutines.runBlocking
import net.folivo.matrix.bot.appservice.room.MatrixRoomService
import net.folivo.matrix.bot.appservice.user.MatrixUserService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

class InitialSyncService(
        private val userService: MatrixUserService,
        private val roomService: MatrixRoomService,
        private val syncService: MatrixSyncService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    @EventListener(ApplicationReadyEvent::class)
    fun initialSync() {
        LOG.info("started initial sync")

        runBlocking {
            LOG.info("delete all users and rooms")
            roomService.deleteAllRooms()
            userService.deleteAllUsers()

            LOG.info("collect all joined rooms (of bot user) - this can take some time!")
            syncService.syncBotMemberships()

            LOG.info("finished initial sync")
        }
    }
}