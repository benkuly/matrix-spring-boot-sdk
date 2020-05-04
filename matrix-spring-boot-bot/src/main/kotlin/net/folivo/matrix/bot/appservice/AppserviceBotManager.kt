package net.folivo.matrix.bot.appservice

import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.user.CreateUserParameter

interface AppserviceBotManager {
    fun shouldCreateUser(matrixUsername: String): Boolean
    fun shouldCreateRoom(matrixRoomAlias: String): Boolean
    fun getCreateRoomParameter(roomAliasName: String): CreateRoomParameter
    fun getCreateUserParameter(username: String): CreateUserParameter
}