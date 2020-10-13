package net.folivo.matrix.bot.appservice.room

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory

class MatrixRoomService(
        private val roomRepository: MatrixRoomRepository,
        private val roomAliasRepository: MatrixRoomAliasRepository
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun getOrCreateRoom(roomId: String): MatrixRoom { //FIXME test
        return roomRepository.findById(roomId).awaitFirstOrNull()
               ?: roomRepository.save(MatrixRoom(roomId)).awaitFirst()
    }

    suspend fun getOrCreateRoomAlias(roomAlias: String, roomId: String): MatrixRoomAlias { //FIXME test
        val existingRoomAlias = roomAliasRepository.findById(roomAlias).awaitFirstOrNull()
        return if (existingRoomAlias != null) {
            if (existingRoomAlias.roomId == roomId) existingRoomAlias
            else roomAliasRepository.save(existingRoomAlias.copy(roomId = roomId)).awaitFirst()
        } else roomAliasRepository.save(MatrixRoomAlias(roomAlias, roomId)).awaitFirst()
    }

    suspend fun existsByRoomAlias(roomAlias: String): Boolean {
        return roomAliasRepository.existsById(roomAlias).awaitFirst()
    }

    fun getRoomsByUserId(userId: String): Flow<MatrixRoom> {
        return roomRepository.findByMember(userId).asFlow()
    }

    fun getRoomsByMembers(members: Set<String>): Flow<MatrixRoom> {
        return roomRepository.findByContainingMembers(members).asFlow()
    }

    suspend fun deleteRoom(roomId: String) {
        roomRepository.deleteById(roomId).awaitFirstOrNull()
    }

    suspend fun deleteAllRooms() {
        roomRepository.deleteAll().awaitFirstOrNull()
    }
}