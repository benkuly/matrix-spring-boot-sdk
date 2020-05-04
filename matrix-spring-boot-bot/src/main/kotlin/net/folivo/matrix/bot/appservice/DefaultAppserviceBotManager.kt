package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.user.CreateUserParameter

open class DefaultAppserviceBotManager : AppserviceBotManager {
    override fun shouldCreateUser(matrixUsername: String): Boolean {
        return true
    }

    override fun shouldCreateRoom(matrixRoomAlias: String): Boolean {
        return false
    }

    override fun getCreateRoomParameter(roomAliasName: String): CreateRoomParameter {
        return CreateRoomParameter()
    }

    override fun getCreateUserParameter(username: String): CreateUserParameter {
        return CreateUserParameter()
    }
}