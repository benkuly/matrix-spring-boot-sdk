package net.folivo.matrix.appservice.api

import net.folivo.matrix.appservice.api.room.AppserviceRoomService
import net.folivo.matrix.appservice.api.user.AppserviceUserService
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.restclient.MatrixClient
import org.slf4j.LoggerFactory

class AppserviceHandlerHelper(
        private val matrixClient: MatrixClient,
        private val appserviceUserService: AppserviceUserService,
        private val appserviceRoomService: AppserviceRoomService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun registerManagedUser(userId: String) {
        try {
            matrixClient.userApi.register(
                    authenticationType = "m.login.application_service",
                    username = userId.trimStart('@').substringBefore(":")
            )
        } catch (error: MatrixServerException) {
            if (error.errorResponse.errorCode == "M_USER_IN_USE") {
                LOG.warn("user $userId has already been created")
            } else throw error
        }
        val displayName = appserviceUserService.getRegisterUserParameter(userId).displayName
        if (displayName != null) {
            matrixClient.userApi.setDisplayName(
                    userId,
                    displayName,
                    asUserId = userId
            )
        }
        appserviceUserService.onRegisteredUser(userId)
    }

    suspend fun createManagedRoom(roomAlias: String) {
        val createRoomParameter = appserviceRoomService.getCreateRoomParameter(roomAlias)
        val roomId = matrixClient.roomsApi
                .createRoom(
                        roomAliasName = roomAlias.trimStart('#').substringBefore(":"),
                        visibility = createRoomParameter.visibility,
                        name = createRoomParameter.name,
                        topic = createRoomParameter.topic,
                        invite = createRoomParameter.invite,
                        invite3Pid = createRoomParameter.invite3Pid,
                        roomVersion = createRoomParameter.roomVersion,
                        asUserId = createRoomParameter.asUserId,
                        creationContent = createRoomParameter.creationContent,
                        initialState = createRoomParameter.initialState,
                        isDirect = createRoomParameter.isDirect,
                        powerLevelContentOverride = createRoomParameter.powerLevelContentOverride,
                        preset = createRoomParameter.preset
                )
        appserviceRoomService.onCreateRoom(roomAlias, roomId)
    }
}