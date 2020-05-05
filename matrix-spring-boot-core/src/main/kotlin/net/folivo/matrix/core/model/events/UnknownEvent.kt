package net.folivo.matrix.core.model.events

class UnknownEvent(type: String?) : Event<UnknownEvent.UnknownEventContent>(type ?: "UNKNOWN", UnknownEventContent()) {

    class UnknownEventContent() : EventContent
}