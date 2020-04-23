package net.folivo.matrix.restclient.api.rooms

enum class Preset(val value: String) {
    PRIVATE("private_chat"),
    PUBLIC("public_chat"),
    TRUSTED_PRIVATE("trusted_private_chat")
}