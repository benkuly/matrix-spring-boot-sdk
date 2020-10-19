package net.folivo.matrix.bot.appservice.room

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.matrix.appservice.api.room.AppserviceRoomService.RoomExistingState.*
import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.bot.util.BotServiceHelper
import net.folivo.matrix.core.model.MatrixId.RoomAliasId
import net.folivo.matrix.core.model.MatrixId.RoomId

class DefaultAppserviceRoomServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val roomServiceMock: MatrixRoomService = mockk(relaxed = true)
        val helperMock: BotServiceHelper = mockk(relaxed = true)

        val cut = DefaultAppserviceRoomService(roomServiceMock, helperMock)

        val roomAlias = RoomAliasId("alias", "server")

        describe(DefaultAppserviceRoomService::roomExistingState.name) {
            it("should return $EXISTS when alias does exists") {
                coEvery { roomServiceMock.existsByRoomAlias(roomAlias) }.returns(true)
                cut.roomExistingState(roomAlias).shouldBe(EXISTS)
            }
            it("should return $CAN_BE_CREATED when alias does not exists but is managed") {
                coEvery { roomServiceMock.existsByRoomAlias(roomAlias) }.returns(false)
                coEvery { helperMock.isManagedRoom(roomAlias) }.returns(true)
                cut.roomExistingState(roomAlias).shouldBe(CAN_BE_CREATED)
            }
            it("should return $DOES_NOT_EXISTS when alias does not exists and is not managed") {
                coEvery { roomServiceMock.existsByRoomAlias(roomAlias) }.returns(false)
                coEvery { helperMock.isManagedRoom(roomAlias) }.returns(false)
                cut.roomExistingState(roomAlias).shouldBe(DOES_NOT_EXISTS)
            }
        }

        describe(DefaultAppserviceRoomService::getCreateRoomParameter.name) {
            it("should return empty ${CreateRoomParameter::class.simpleName}") {
                cut.getCreateRoomParameter(roomAlias).shouldBe(CreateRoomParameter())
            }
        }

        describe(DefaultAppserviceRoomService::onCreatedRoom.name) {
            val room = RoomId("room", "server")
            it("should save room alias") {
                cut.onCreatedRoom(roomAlias, room)
                coVerify { roomServiceMock.getOrCreateRoomAlias(roomAlias, room) }
            }
        }
    }
}