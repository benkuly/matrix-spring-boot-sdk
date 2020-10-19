package net.folivo.matrix.bot.appservice.user

import net.folivo.matrix.appservice.api.user.AppserviceUserService
import net.folivo.matrix.appservice.api.user.AppserviceUserService.UserExistingState
import net.folivo.matrix.appservice.api.user.AppserviceUserService.UserExistingState.*
import net.folivo.matrix.appservice.api.user.RegisterUserParameter
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.core.model.MatrixId.UserId

open class DefaultAppserviceUserService(
        private val userService: MatrixUserService,
        private val helper: BotServiceHelper,
        private val botProperties: MatrixBotProperties
) : AppserviceUserService {

    override suspend fun userExistingState(userId: UserId): UserExistingState {
        val userExists = userService.existsUser(userId)
        return if (userExists) EXISTS
        else if (helper.isManagedUser(userId)) CAN_BE_CREATED else DOES_NOT_EXISTS

    }

    override suspend fun getRegisterUserParameter(userId: UserId): RegisterUserParameter {
        return if (userId == helper.getBotUserId()) {
            RegisterUserParameter(botProperties.displayName)
        } else {
            RegisterUserParameter()
        }
    }

    override suspend fun onRegisteredUser(userId: UserId) {
        userService.getOrCreateUser(userId)
    }
}