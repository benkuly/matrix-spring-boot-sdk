package net.folivo.matrix.appservice

import net.folivo.matrix.appservice.api.event.MatrixAppserviceEventService
import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.appservice.api.user.CreateUserParameter
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.core.model.events.Event
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RequestPredicates.accept
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse


@SpringBootConfiguration
@EnableAutoConfiguration
class TestApplication {

    @Bean
    fun something(): RouterFunction<ServerResponse> {
        return route(GET("/_matrix/something").and(accept(APPLICATION_JSON)), HandlerFunction {
            ServerResponse.ok().build()
        })
    }

    @Bean
    fun noOpMatrixAppserviceEventService(): MatrixAppserviceEventService {
        return object : MatrixAppserviceEventService {
            override fun eventProcessingState(
                    tnxId: String,
                    eventIdOrType: String
            ): MatrixAppserviceEventService.EventProcessingState {
                return MatrixAppserviceEventService.EventProcessingState.PROCESSED
            }

            override fun saveEventProcessed(tnxId: String, eventIdOrType: String) {
            }

            override fun processEvent(event: Event<*>) {
            }
        }
    }

    @Bean
    fun noOpMatrixAppserviceRoomService(): MatrixAppserviceRoomService {
        return object : MatrixAppserviceRoomService {
            override fun roomExistingState(roomAlias: String): MatrixAppserviceRoomService.RoomExistingState {
                return MatrixAppserviceRoomService.RoomExistingState.DOES_NOT_EXISTS
            }

            override fun getCreateRoomParameter(roomAlias: String): CreateRoomParameter {
                return CreateRoomParameter()
            }

            override fun saveRoom(roomAlias: String) {
            }
        }
    }

    @Bean
    fun noOpMatrixAppserviceUserService(): MatrixAppserviceUserService {
        return object : MatrixAppserviceUserService {
            override fun userExistingState(userId: String): MatrixAppserviceUserService.UserExistingState {
                return MatrixAppserviceUserService.UserExistingState.DOES_NOT_EXISTS
            }

            override fun getCreateUserParameter(userId: String): CreateUserParameter {
                return CreateUserParameter()
            }

            override fun saveUser(userId: String) {
            }
        }
    }

}