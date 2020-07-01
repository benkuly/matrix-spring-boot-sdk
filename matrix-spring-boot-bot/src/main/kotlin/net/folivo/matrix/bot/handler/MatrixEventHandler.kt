package net.folivo.matrix.bot.handler

import net.folivo.matrix.core.model.events.Event

interface MatrixEventHandler {

    fun supports(clazz: Class<*>): Boolean

    suspend fun handleEvent(event: Event<*>, roomId: String? = null)
}