package net.folivo.spring.matrix.bot.event

import liquibase.pro.packaged.T
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.EventContent
import kotlin.reflect.KClass

interface MatrixEventHandler<T : EventContent> {

    suspend fun supports(): KClass<T>

    suspend fun handleEvent(event: Event<out T>)
}