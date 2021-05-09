package net.folivo.spring.matrix.appservice

import net.folivo.trixnity.appservice.rest.event.AppserviceEventTnxService
import net.folivo.trixnity.appservice.rest.event.AppserviceEventTnxService.EventTnxProcessingState.PROCESSED
import net.folivo.trixnity.appservice.rest.room.AppserviceRoomService
import net.folivo.trixnity.appservice.rest.room.CreateRoomParameter
import net.folivo.trixnity.appservice.rest.user.AppserviceUserService
import net.folivo.trixnity.appservice.rest.user.RegisterUserParameter
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.core.model.MatrixId
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class TestConfiguration {

    @Bean
    fun noOpAppserviceEventService(): AppserviceEventTnxService {
        return object : AppserviceEventTnxService {
            override suspend fun eventTnxProcessingState(tnxId: String): AppserviceEventTnxService.EventTnxProcessingState {
                return PROCESSED
            }

            override suspend fun onEventTnxProcessed(tnxId: String) {
            }

        }
    }

    @Bean
    fun noOpAppserviceRoomService(matrixClient: MatrixClient): AppserviceRoomService {
        return object : AppserviceRoomService {
            override val matrixClient: MatrixClient
                get() = matrixClient

            override suspend fun roomExistingState(roomAlias: MatrixId.RoomAliasId): AppserviceRoomService.RoomExistingState {
                return AppserviceRoomService.RoomExistingState.DOES_NOT_EXISTS
            }

            override suspend fun getCreateRoomParameter(roomAlias: MatrixId.RoomAliasId): CreateRoomParameter {
                return CreateRoomParameter()
            }

            override suspend fun onCreatedRoom(roomAlias: MatrixId.RoomAliasId, roomId: MatrixId.RoomId) {

            }
        }
    }

    @Bean
    fun noOpAppserviceUserService(matrixClient: MatrixClient): AppserviceUserService {
        return object : AppserviceUserService {
            override val matrixClient: MatrixClient
                get() = matrixClient

            override suspend fun userExistingState(userId: MatrixId.UserId): AppserviceUserService.UserExistingState {
                return AppserviceUserService.UserExistingState.DOES_NOT_EXISTS
            }

            override suspend fun getRegisterUserParameter(userId: MatrixId.UserId): RegisterUserParameter {
                return RegisterUserParameter()
            }

            override suspend fun onRegisteredUser(userId: MatrixId.UserId) {

            }
        }
    }

}