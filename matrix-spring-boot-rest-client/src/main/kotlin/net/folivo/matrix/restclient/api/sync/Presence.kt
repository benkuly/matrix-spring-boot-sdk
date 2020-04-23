package net.folivo.matrix.restclient.api.sync

enum class Presence(val value: String) {
    OFFLINE("offline"),
    ONLINE("online"),
    UNAVAILABLE("unavailable"),
}