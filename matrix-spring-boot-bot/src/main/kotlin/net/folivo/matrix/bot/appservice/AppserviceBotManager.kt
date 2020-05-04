package net.folivo.matrix.bot.appservice

interface AppserviceBotManager {
    fun shouldCreateUser(matrixUsername: String): Boolean
}