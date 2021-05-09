package net.folivo.spring.matrix.bot.room

import kotlinx.coroutines.flow.Flow
import net.folivo.trixnity.core.model.MatrixId

class MatrixRoomService(
    private val roomRepository: MatrixRoomRepository,
    private val roomAliasRepository: MatrixRoomAliasRepository
) {
    suspend fun getOrCreateRoom(roomId: MatrixId.RoomId): MatrixRoom {
        return roomRepository.findById(roomId)
            ?: roomRepository.save(MatrixRoom(roomId))
    }

    suspend fun getOrCreateRoomAlias(roomAlias: MatrixId.RoomAliasId, roomId: MatrixId.RoomId): MatrixRoomAlias {
        roomRepository.findById(roomId) ?: roomRepository.save(MatrixRoom(roomId, true))
        val existingRoomAlias = roomAliasRepository.findById(roomAlias)
        return if (existingRoomAlias != null) {
            if (existingRoomAlias.roomId == roomId) existingRoomAlias
            else roomAliasRepository.save(existingRoomAlias.copy(roomId = roomId))
        } else {
            roomAliasRepository.save(MatrixRoomAlias(roomAlias, roomId))
        }
    }

    suspend fun getRoomAliasByRoomId(roomId: MatrixId.RoomId): MatrixRoomAlias? {
        return roomAliasRepository.findByRoomId(roomId)
    }

    suspend fun getRoomAlias(roomAlias: MatrixId.RoomAliasId): MatrixRoomAlias? {
        return roomAliasRepository.findById(roomAlias)
    }

    suspend fun existsByRoomAlias(roomAlias: MatrixId.RoomAliasId): Boolean {
        return roomAliasRepository.existsById(roomAlias)
    }

    fun getRoomsByMembers(members: Set<MatrixId.UserId>): Flow<MatrixRoom> {
        return roomRepository.findByMembers(members)
    }

    suspend fun deleteRoom(roomId: MatrixId.RoomId) {
        roomRepository.deleteById(roomId)
    }

    suspend fun deleteAllRooms() {
        roomRepository.deleteAll()
    }
}