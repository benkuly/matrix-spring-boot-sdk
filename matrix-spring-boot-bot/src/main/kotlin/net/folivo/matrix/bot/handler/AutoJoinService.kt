package net.folivo.matrix.bot.handler

import reactor.core.publisher.Mono

interface AutoJoinService {
    fun shouldJoin(roomId: String, userId: String?, isAsUser: Boolean = false): Mono<Boolean>
}