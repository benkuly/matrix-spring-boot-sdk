package net.folivo.matrix.bot.event

import net.folivo.matrix.core.model.events.Event

interface MatrixEventHandler {

    suspend fun supports(clazz: Class<*>): Boolean

    suspend fun handleEvent(event: Event<*>, roomId: String? = null)
}