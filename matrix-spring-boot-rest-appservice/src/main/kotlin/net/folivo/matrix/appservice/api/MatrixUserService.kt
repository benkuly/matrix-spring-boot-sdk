package net.folivo.matrix.appservice.api

interface MatrixUserService {
    fun hasUser(userId: String): Boolean
    fun shouldCreateUser(userId: String): Boolean
}