package net.folivo.spring.matrix.bot.appservice

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.spring.matrix.bot.room.MatrixRoomService
import net.folivo.spring.matrix.bot.util.BotServiceHelper
import net.folivo.trixnity.appservice.rest.room.AppserviceRoomService.RoomExistingState.*
import net.folivo.trixnity.appservice.rest.room.CreateRoomParameter
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.core.model.MatrixId

class DefaultAppserviceRoomServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val roomServiceMock: MatrixRoomService = mockk(relaxed = true)
        val helperMock: BotServiceHelper = mockk(relaxed = true)
        val matrixClientMock: MatrixClient = mockk(relaxed = true)

        val cut = DefaultAppserviceRoomService(roomServiceMock, helperMock, matrixClientMock)

        val roomAlias = MatrixId.RoomAliasId("alias", "server")

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
            val room = MatrixId.RoomId("room", "server")
            it("should save room alias") {
                cut.onCreatedRoom(roomAlias, room)
                coVerify { roomServiceMock.getOrCreateRoomAlias(roomAlias, room) }
            }
        }
    }
}