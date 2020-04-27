package net.folivo.matrix.common.model.events

class UnknownEvent(type: String) : Event<UnknownEvent.UnknownEventContent>(type, UnknownEventContent()) {

    class UnknownEventContent() : EventContent
}