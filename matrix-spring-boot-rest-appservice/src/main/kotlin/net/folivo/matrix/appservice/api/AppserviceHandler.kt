package net.folivo.matrix.appservice.api

import kotlinx.coroutines.flow.Flow
import net.folivo.matrix.core.model.events.Event

interface AppserviceHandler {
    suspend fun addTransactions(tnxId: String, events: Flow<Event<*>>)
    suspend fun hasUser(userId: String): Boolean
    suspend fun hasRoomAlias(roomAlias: String): Boolean
}