package net.folivo.matrix.bot.appservice.user

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AppserviceUserRepository : ReactiveCrudRepository<AppserviceUser, String> {
}