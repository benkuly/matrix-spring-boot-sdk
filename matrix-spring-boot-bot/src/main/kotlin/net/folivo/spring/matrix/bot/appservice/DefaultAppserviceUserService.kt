package net.folivo.spring.matrix.bot.appservice

import net.folivo.spring.matrix.bot.config.MatrixBotProperties
import net.folivo.spring.matrix.bot.user.MatrixUserService
import net.folivo.spring.matrix.bot.util.BotServiceHelper
import net.folivo.trixnity.appservice.rest.user.AppserviceUserService
import net.folivo.trixnity.appservice.rest.user.AppserviceUserService.UserExistingState.*
import net.folivo.trixnity.appservice.rest.user.RegisterUserParameter
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.core.model.MatrixId

open class DefaultAppserviceUserService(
    private val userService: MatrixUserService,
    private val helper: BotServiceHelper,
    private val botProperties: MatrixBotProperties,
    override val matrixClient: MatrixClient
) : AppserviceUserService {

    override suspend fun userExistingState(userId: MatrixId.UserId): AppserviceUserService.UserExistingState {
        val userExists = userService.existsUser(userId)
        return if (userExists) EXISTS
        else if (helper.isManagedUser(userId)) CAN_BE_CREATED else DOES_NOT_EXISTS

    }

    override suspend fun getRegisterUserParameter(userId: MatrixId.UserId): RegisterUserParameter {
        return if (userId == botProperties.botUserId) {
            RegisterUserParameter(botProperties.displayName)
        } else {
            RegisterUserParameter()
        }
    }

    override suspend fun onRegisteredUser(userId: MatrixId.UserId) {
        userService.getOrCreateUser(userId)
    }
}