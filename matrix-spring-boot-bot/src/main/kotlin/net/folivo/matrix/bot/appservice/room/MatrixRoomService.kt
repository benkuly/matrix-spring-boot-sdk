package net.folivo.matrix.bot.appservice.room

import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory

class MatrixRoomService(
        private val roomRepository: MatrixRoomRepository,
        private val roomAliasRepository: MatrixRoomAliasRepository
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun getOrCreateRoom(roomId: String): MatrixRoom { //FIXME test
        return roomRepository.findById(roomId)
               ?: roomRepository.save(MatrixRoom(roomId))
    }

    suspend fun getOrCreateRoomAlias(roomAlias: String, roomId: String): MatrixRoomAlias { //FIXME test
        roomRepository.findById(roomId) ?: roomRepository.save(MatrixRoom(roomId, true))
        val existingRoomAlias = roomAliasRepository.findById(roomAlias)
        return if (existingRoomAlias != null) {
            if (existingRoomAlias.roomId == roomId) existingRoomAlias
            else roomAliasRepository.save(existingRoomAlias.copy(roomId = roomId))
        } else {
            roomAliasRepository.save(MatrixRoomAlias(roomAlias, roomId))
        }
    }

    suspend fun existsByRoomAlias(roomAlias: String): Boolean {
        return roomAliasRepository.existsById(roomAlias)
    }

    fun getRoomsByUserId(userId: String): Flow<MatrixRoom> {
        return roomRepository.findByMember(userId)
    }

    fun getRoomsByMembers(members: Set<String>): Flow<MatrixRoom> {
        return roomRepository.findByContainingMembers(members)
    }

    suspend fun deleteRoom(roomId: String) {
        roomRepository.deleteById(roomId)
    }

    suspend fun deleteAllRooms() {
        roomRepository.deleteAll()
    }
}