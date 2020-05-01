package net.folivo.matrix.appservice.api

import net.folivo.matrix.core.model.events.StateEvent
import net.folivo.matrix.core.model.events.m.room.CreateEvent
import net.folivo.matrix.core.model.events.m.room.PowerLevelsEvent
import net.folivo.matrix.restclient.api.rooms.Invite3Pid
import net.folivo.matrix.restclient.api.rooms.Preset
import net.folivo.matrix.restclient.api.rooms.Visibility

data class CreateRoomParameter(
        val visibility: Visibility = Visibility.PUBLIC,
        val name: String? = null,
        val topic: String? = null,
        val invite: List<String>? = null,
        val invite3Pid: List<Invite3Pid>? = null,
        val roomVersion: String? = null,
        val creationContent: CreateEvent.CreateEventContent? = null,
        val initialState: List<StateEvent<*, *>>? = null,
        val preset: Preset? = null,
        val isDirect: Boolean? = null,
        val powerLevelContentOverride: PowerLevelsEvent.PowerLevelsEventContent? = null,
        val asUserId: String? = null
)