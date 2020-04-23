package net.folivo.matrix.bot.handler

import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.model.events.Event

interface MatrixEventHandler {

    fun supports(clazz: Class<*>): Boolean

    fun handleEvent(event: Event<*>, roomId: String, matrixClient: MatrixClient)
}