package net.folivo.matrix.bot.appservice.room

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface AppserviceRoomRepository : ReactiveCrudRepository<AppserviceRoom, String> {
    fun findByRoomAlias(roomAlias: String): Mono<AppserviceRoom>
    fun existsByRoomAlias(roomAlias: String): Mono<Boolean>
}