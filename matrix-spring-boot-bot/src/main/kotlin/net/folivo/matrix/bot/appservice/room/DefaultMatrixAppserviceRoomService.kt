package net.folivo.matrix.bot.appservice.room

import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService.RoomExistingState
import net.folivo.matrix.bot.appservice.AppserviceBotManager
import net.folivo.matrix.bot.appservice.user.AppserviceUser
import net.folivo.matrix.bot.appservice.user.AppserviceUserRepository
import reactor.core.publisher.Mono

class DefaultMatrixAppserviceRoomService(
        private val appserviceBotManager: AppserviceBotManager,
        private val appserviceRoomRepository: AppserviceRoomRepository,
        private val appserviceUserRepository: AppserviceUserRepository
) : MatrixAppserviceRoomService {

    override fun roomExistingState(roomAlias: String): Mono<RoomExistingState> {
        return appserviceRoomRepository.findByRoomAlias(roomAlias)
                .map { RoomExistingState.EXISTS }
                .switchIfEmpty(
                        Mono.defer {
                            appserviceBotManager.shouldCreateRoom(roomAlias)
                                    .map { shouldCreateRoom ->
                                        if (shouldCreateRoom) {
                                            RoomExistingState.CAN_BE_CREATED
                                        } else {
                                            RoomExistingState.DOES_NOT_EXISTS
                                        }
                                    }
                        }
                )
    }

    override fun getCreateRoomParameter(roomAlias: String): Mono<CreateRoomParameter> {
        return appserviceBotManager.getCreateRoomParameter(roomAlias)
    }

    override fun saveRoom(roomAlias: String, roomId: String): Mono<Void> {
        return appserviceRoomRepository.save(AppserviceRoom(roomId, roomAlias))
                .then()
    }

    fun saveRoomJoin(roomId: String, userId: String): Mono<Void> {
        return appserviceRoomRepository.findById(roomId)
                .switchIfEmpty(appserviceRoomRepository.save(AppserviceRoom(roomId)))
                .zipWith(
                        appserviceUserRepository.findById(userId)
                                .switchIfEmpty(Mono.just(AppserviceUser(userId)))
                ).flatMap {
                    val room = it.t1
                    val user = it.t2
                    user.rooms.add(room)
                    appserviceUserRepository.save(user)
                }.then()
    }

    fun saveRoomLeave(roomId: String, userId: String): Mono<Void> {
        return appserviceUserRepository.findById(userId)
                .flatMap { user ->
                    user.rooms.removeIf { it.roomId == roomId }
                    appserviceUserRepository.save(user)
                }.then()
    }
}