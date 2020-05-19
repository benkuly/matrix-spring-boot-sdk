package net.folivo.matrix.bot.handler

import reactor.core.publisher.Mono

class DefaultAutoJoinService : AutoJoinService {
    override fun shouldJoin(roomId: String, userId: String?, isAsUser: Boolean): Mono<Boolean> {
        return Mono.just(true)
    }
}