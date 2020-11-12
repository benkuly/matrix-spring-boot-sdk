package net.folivo.matrix.bot.room

import kotlinx.coroutines.flow.Flow
import net.folivo.matrix.core.model.MatrixId.*
import org.slf4j.LoggerFactory

class MatrixRoomService(
        private val roomRepository: MatrixRoomRepository,
        private val roomAliasRepository: MatrixRoomAliasRepository
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun getOrCreateRoom(roomId: RoomId): MatrixRoom {
        return roomRepository.findById(roomId)
               ?: roomRepository.save(MatrixRoom(roomId))
    }

    suspend fun getOrCreateRoomAlias(roomAlias: RoomAliasId, roomId: RoomId): MatrixRoomAlias {
        roomRepository.findById(roomId) ?: roomRepository.save(MatrixRoom(roomId, true))
        val existingRoomAlias = roomAliasRepository.findById(roomAlias)
        return if (existingRoomAlias != null) {
            if (existingRoomAlias.roomId == roomId) existingRoomAlias
            else roomAliasRepository.save(existingRoomAlias.copy(roomId = roomId))
        } else {
            roomAliasRepository.save(MatrixRoomAlias(roomAlias, roomId))
        }
    }

    suspend fun getRoomAlias(roomAlias: RoomAliasId): MatrixRoomAlias? {
        return roomAliasRepository.findById(roomAlias)
    }

    suspend fun existsByRoomAlias(roomAlias: RoomAliasId): Boolean {
        return roomAliasRepository.existsById(roomAlias)
    }

    fun getRoomsByMembers(members: Set<UserId>): Flow<MatrixRoom> {
        return roomRepository.findByMembers(members)
    }

    suspend fun deleteRoom(roomId: RoomId) {
        roomRepository.deleteById(roomId)
    }

    suspend fun deleteAllRooms() {
        roomRepository.deleteAll()
    }
}