package net.folivo.matrix.core.handler

import net.folivo.matrix.core.model.events.Event

interface MatrixEventHandler {

    fun supports(clazz: Class<*>): Boolean

    fun handleEvent(event: Event<*>, roomId: String? = null)
}