package net.folivo.matrix.bot.appservice.user

import net.folivo.matrix.appservice.api.user.AppserviceUserService
import net.folivo.matrix.appservice.api.user.AppserviceUserService.UserExistingState
import net.folivo.matrix.appservice.api.user.AppserviceUserService.UserExistingState.*
import net.folivo.matrix.appservice.api.user.RegisterUserParameter
import net.folivo.matrix.bot.config.MatrixBotProperties
import net.folivo.matrix.bot.util.BotServiceHelper

open class DefaultAppserviceUserService(
        private val userService: MatrixUserService,
        private val helper: BotServiceHelper,
        private val botProperties: MatrixBotProperties
) : AppserviceUserService {

    override suspend fun userExistingState(userId: String): UserExistingState {
        val userExists = userService.existsUser(userId)
        return if (userExists) EXISTS
        else if (helper.isManagedUser(userId)) CAN_BE_CREATED else DOES_NOT_EXISTS

    }

    override suspend fun getRegisterUserParameter(userId: String): RegisterUserParameter {
        return if (userId == helper.getBotUserId()) {
            RegisterUserParameter(botProperties.displayName)
        } else {
            RegisterUserParameter()
        }
    }

    override suspend fun onRegisteredUser(userId: String) {
        userService.getOrCreateUser(userId)
    }
}