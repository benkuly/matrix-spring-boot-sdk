package net.folivo.matrix.bot.appservice.room

import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.bot.appservice.AppserviceBotManager
import net.folivo.matrix.bot.appservice.user.AppserviceUser
import net.folivo.matrix.bot.appservice.user.AppserviceUserRepository
import org.springframework.data.repository.findByIdOrNull
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

// FIXME test
class DefaultMatrixAppserviceRoomService(
        private val appserviceBotManager: AppserviceBotManager,
        private val appserviceRoomRepository: AppserviceRoomRepository,
        private val appserviceUserRepository: AppserviceUserRepository
) : MatrixAppserviceRoomService {

    override fun roomExistingState(roomAlias: String): Mono<MatrixAppserviceRoomService.RoomExistingState> {
        return Mono.fromCallable { appserviceRoomRepository.findByRoomAlias(roomAlias) == null }
                .subscribeOn(Schedulers.boundedElastic())
                .concatWith { appserviceBotManager.shouldCreateRoom(roomAlias) }
                .all { it }
                .map {
                    if (it) {
                        MatrixAppserviceRoomService.RoomExistingState.CAN_BE_CREATED
                    } else {
                        MatrixAppserviceRoomService.RoomExistingState.DOES_NOT_EXISTS
                    }
                }
    }

    override fun getCreateRoomParameter(roomAlias: String): Mono<CreateRoomParameter> {
        return appserviceBotManager.getCreateRoomParameter(roomAlias)
    }

    override fun saveRoom(roomAlias: String, roomId: String): Mono<Void> {
        return Mono.fromCallable {
            appserviceRoomRepository.save(AppserviceRoom(roomId, roomAlias))
        }.subscribeOn(Schedulers.boundedElastic())
                .then()
    }

    fun saveRoomJoin(roomId: String, userId: String): Mono<Void> {
        return Mono.fromCallable {
            val room = appserviceRoomRepository.findByIdOrNull(roomId)
                       ?: appserviceRoomRepository.save(AppserviceRoom(roomId))
            val user = appserviceUserRepository.findByIdOrNull(userId)
                       ?: appserviceUserRepository.save(AppserviceUser(userId))
            room.members.add(user)
            user.rooms.add(room)
            appserviceRoomRepository.save(room)
            appserviceUserRepository.save(user)
        }.subscribeOn(Schedulers.boundedElastic())
                .then()
    }
}