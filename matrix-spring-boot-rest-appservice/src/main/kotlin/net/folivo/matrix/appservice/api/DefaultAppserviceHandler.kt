package net.folivo.matrix.appservice.api

import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService.RoomExistingState
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService.UserExistingState
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.RoomEvent
import net.folivo.matrix.core.model.events.StateEvent
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class DefaultAppserviceHandler(
        private val matrixAppserviceEventService: MatrixAppserviceEventService,
        private val matrixAppserviceUserService: MatrixAppserviceUserService,
        private val matrixAppserviceRoomService: MatrixAppserviceRoomService,
        private val helper: AppserviceHandlerHelper
) : AppserviceHandler {

    private val logger = LoggerFactory.getLogger(DefaultAppserviceHandler::class.java)

    override fun addTransactions(tnxId: String, events: Flux<Event<*>>): Mono<Void> {
        return events.flatMap { event ->
            val eventIdOrType = when (event) {
                is RoomEvent<*, *>  -> event.id
                is StateEvent<*, *> -> event.id
                else                -> event.type
            }
            logger.debug("incoming event $eventIdOrType in transaction $tnxId")
            matrixAppserviceEventService.eventProcessingState(tnxId, eventIdOrType)
                    .flatMap { eventProcessingState ->
                        when (eventProcessingState) {
                            MatrixAppserviceEventService.EventProcessingState.NOT_PROCESSED -> {
                                logger.debug("process event $eventIdOrType in transaction $tnxId")
                                matrixAppserviceEventService.processEvent(event)
                                        .thenReturn(true)//TODO fix this hacky workaround (without this saveEventProcessed never gets called due to empty mono
                                        .flatMap {
                                            matrixAppserviceEventService.saveEventProcessed(
                                                    tnxId,
                                                    eventIdOrType
                                            )
                                        }
                            }
                            MatrixAppserviceEventService.EventProcessingState.PROCESSED     -> {
                                logger.debug("event $eventIdOrType in transaction $tnxId already processed")
                                Mono.empty()
                            }
                            else                                                            -> {
                                Mono.empty()
                            }
                        }
                    }
        }.doOnError { logger.error("something went wrong while processing events", it) }.then()
    }

    override fun hasUser(userId: String): Mono<Boolean> {
        return matrixAppserviceUserService.userExistingState(userId)
                .flatMap { userExistingState ->
                    when (userExistingState) {
                        UserExistingState.EXISTS          -> Mono.just(true)
                        UserExistingState.DOES_NOT_EXISTS -> Mono.just(false)
                        UserExistingState.CAN_BE_CREATED  -> {
                            logger.debug("started user creation of $userId")
                            helper.registerAndSaveUser(userId)
                        }
                        else                              -> {
                            Mono.just(false)
                        }
                    }
                }
    }

    override fun hasRoomAlias(roomAlias: String): Mono<Boolean> {
        return matrixAppserviceRoomService.roomExistingState(roomAlias)
                .flatMap { roomExistingState ->
                    when (roomExistingState) {
                        RoomExistingState.EXISTS          -> Mono.just(true)
                        RoomExistingState.DOES_NOT_EXISTS -> Mono.just(false)
                        RoomExistingState.CAN_BE_CREATED  -> {
                            logger.debug("started room creation of $roomAlias")
                            helper.createAndSaveRoom(roomAlias)
                        }
                        else                              -> {
                            Mono.just(false)
                        }
                    }
                }
    }
}