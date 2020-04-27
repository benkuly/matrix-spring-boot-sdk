package net.folivo.matrix.bot.handler

import net.folivo.matrix.common.model.events.Event
import net.folivo.matrix.restclient.MatrixClient

interface MatrixEventHandler {

    fun supports(clazz: Class<*>): Boolean

    fun handleEvent(event: Event<*>, roomId: String, matrixClient: MatrixClient)
}