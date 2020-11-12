package net.folivo.matrix.appservice.api

import net.folivo.matrix.appservice.api.room.AppserviceRoomService
import net.folivo.matrix.appservice.api.user.AppserviceUserService
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.core.model.MatrixId.RoomAliasId
import net.folivo.matrix.core.model.MatrixId.UserId
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

    suspend fun registerManagedUser(userId: UserId) {
        LOG.debug("try to register user")
        try {
            matrixClient.userApi.register(
                    authenticationType = "m.login.application_service",
                    username = userId.localpart
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
        LOG.debug("registered user")
        appserviceUserService.onRegisteredUser(userId)
    }

    suspend fun createManagedRoom(roomAlias: RoomAliasId) {
        LOG.debug("try to create room")
        val createRoomParameter = appserviceRoomService.getCreateRoomParameter(roomAlias)
        val roomId = matrixClient.roomsApi
                .createRoom(
                        roomAliasId = roomAlias,
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
        LOG.debug("created room")
        appserviceRoomService.onCreatedRoom(roomAlias, roomId)
    }
}