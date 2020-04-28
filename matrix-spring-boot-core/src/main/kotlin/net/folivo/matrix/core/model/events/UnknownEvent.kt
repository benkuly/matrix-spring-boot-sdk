package net.folivo.matrix.core.model.events

class UnknownEvent(type: String) : Event<UnknownEvent.UnknownEventContent>(type, UnknownEventContent()) {

    class UnknownEventContent() : EventContent
}