package net.folivo.matrix.appservice.api

import kotlinx.coroutines.flow.Flow
import net.folivo.matrix.core.model.MatrixId.RoomAliasId
import net.folivo.matrix.core.model.MatrixId.UserId
import net.folivo.matrix.core.model.events.Event

interface AppserviceHandler {
    suspend fun addTransactions(tnxId: String, events: Flow<Event<*>>)
    suspend fun hasUser(userId: UserId): Boolean
    suspend fun hasRoomAlias(roomAlias: RoomAliasId): Boolean
}