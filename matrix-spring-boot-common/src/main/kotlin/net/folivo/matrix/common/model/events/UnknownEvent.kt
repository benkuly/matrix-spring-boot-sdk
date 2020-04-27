package net.folivo.matrix.restclient.model.events

class UnknownEvent(type: String) : Event<UnknownEvent.UnknownEventContent>(type, UnknownEventContent()) {

    class UnknownEventContent() : EventContent
}