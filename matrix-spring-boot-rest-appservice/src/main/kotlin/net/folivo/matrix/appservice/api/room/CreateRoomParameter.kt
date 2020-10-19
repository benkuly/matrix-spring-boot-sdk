package net.folivo.matrix.appservice.api.room

import net.folivo.matrix.core.model.MatrixId.UserId
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
        val invite: Set<UserId>? = null,
        val invite3Pid: Set<Invite3Pid>? = null,
        val roomVersion: String? = null,
        val creationContent: CreateEvent.CreateEventContent? = null,
        val initialState: List<StateEvent<*, *>>? = null,
        val preset: Preset? = null,
        val isDirect: Boolean? = null,
        val powerLevelContentOverride: PowerLevelsEvent.PowerLevelsEventContent? = null,
        val asUserId: UserId? = null // FIXME why?
)