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
import reactor.core.publisher.Mono


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
    fun testController(): TestRestController {
        return TestRestController()
    }

    @Bean
    fun noOpMatrixAppserviceEventService(): MatrixAppserviceEventService {
        return object : MatrixAppserviceEventService {
            override fun eventProcessingState(
                    tnxId: String,
                    eventIdOrType: String
            ): Mono<MatrixAppserviceEventService.EventProcessingState> {
                return Mono.just(MatrixAppserviceEventService.EventProcessingState.PROCESSED)
            }

            override fun saveEventProcessed(tnxId: String, eventIdOrType: String): Mono<Void> {
                return Mono.empty()
            }

            override fun processEvent(event: Event<*>): Mono<Void> {
                return Mono.empty()
            }
        }
    }

    @Bean
    fun noOpMatrixAppserviceRoomService(): MatrixAppserviceRoomService {
        return object : MatrixAppserviceRoomService {
            override fun roomExistingState(roomAlias: String): Mono<MatrixAppserviceRoomService.RoomExistingState> {
                return Mono.just(MatrixAppserviceRoomService.RoomExistingState.DOES_NOT_EXISTS)
            }

            override fun getCreateRoomParameter(roomAlias: String): Mono<CreateRoomParameter> {
                return Mono.just(CreateRoomParameter())
            }

            override fun saveRoom(roomAlias: String, roomId: String): Mono<Void> {
                return Mono.empty()
            }
        }
    }

    @Bean
    fun noOpMatrixAppserviceUserService(): MatrixAppserviceUserService {
        return object : MatrixAppserviceUserService {
            override fun userExistingState(userId: String): Mono<MatrixAppserviceUserService.UserExistingState> {
                return Mono.just(MatrixAppserviceUserService.UserExistingState.DOES_NOT_EXISTS)
            }

            override fun getCreateUserParameter(userId: String): Mono<CreateUserParameter> {
                return Mono.just(CreateUserParameter())
            }

            override fun saveUser(userId: String): Mono<Void> {
                return Mono.empty()
            }
        }
    }

}