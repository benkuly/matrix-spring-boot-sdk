package net.folivo.matrix.bot.handler

import net.folivo.matrix.core.model.events.Event
import reactor.core.publisher.Mono

interface MatrixEventHandler {

    fun supports(clazz: Class<*>): Boolean

    fun handleEvent(event: Event<*>, roomId: String? = null): Mono<Void>
}