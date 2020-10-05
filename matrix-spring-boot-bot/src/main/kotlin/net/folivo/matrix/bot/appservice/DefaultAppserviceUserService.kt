package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.user.AppserviceUserService
import net.folivo.matrix.appservice.api.user.AppserviceUserService.UserExistingState
import net.folivo.matrix.appservice.api.user.AppserviceUserService.UserExistingState.CAN_BE_CREATED
import net.folivo.matrix.appservice.api.user.AppserviceUserService.UserExistingState.EXISTS
import net.folivo.matrix.appservice.api.user.RegisterUserParameter
import net.folivo.matrix.bot.handler.BotServiceHelper

open class DefaultAppserviceUserService(
        private val helper: BotServiceHelper
) : AppserviceUserService {
    override suspend fun userExistingState(userId: String): UserExistingState {
        return if (helper.isManagedUser(userId)) CAN_BE_CREATED else EXISTS
    }

    override suspend fun getRegisterUserParameter(userId: String): RegisterUserParameter {
        return RegisterUserParameter()
    }

    override suspend fun onRegisteredUser(userId: String) {
    }
}