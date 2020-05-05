package net.folivo.matrix.bot.appservice.room

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AppserviceRoomRepository : CrudRepository<AppserviceRoom, String> {
    fun findByRoomAlias(roomAlias: String): AppserviceRoom?
}