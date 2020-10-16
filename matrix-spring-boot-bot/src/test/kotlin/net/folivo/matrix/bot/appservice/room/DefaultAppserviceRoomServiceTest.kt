package net.folivo.matrix.bot.appservice.room

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.matrix.appservice.api.room.AppserviceRoomService.RoomExistingState.*
import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.bot.util.BotServiceHelper

class DefaultAppserviceRoomServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val roomServiceMock: MatrixRoomService = mockk(relaxed = true)
        val helperMock: BotServiceHelper = mockk(relaxed = true)

        val cut = DefaultAppserviceRoomService(roomServiceMock, helperMock)

        describe(DefaultAppserviceRoomService::roomExistingState.name) {
            it("should return $EXISTS when alias does exists") {
                coEvery { roomServiceMock.existsByRoomAlias("someAlias") }.returns(true)
                cut.roomExistingState("someAlias").shouldBe(EXISTS)
            }
            it("should return $CAN_BE_CREATED when alias does not exists but is managed") {
                coEvery { roomServiceMock.existsByRoomAlias("someAlias") }.returns(false)
                coEvery { helperMock.isManagedRoom("someAlias") }.returns(true)
                cut.roomExistingState("someAlias").shouldBe(CAN_BE_CREATED)
            }
            it("should return $DOES_NOT_EXISTS when alias does not exists and is not managed") {
                coEvery { roomServiceMock.existsByRoomAlias("someAlias") }.returns(false)
                coEvery { helperMock.isManagedRoom("someAlias") }.returns(false)
                cut.roomExistingState("someAlias").shouldBe(DOES_NOT_EXISTS)
            }
        }

        describe(DefaultAppserviceRoomService::getCreateRoomParameter.name) {
            it("should return empty ${CreateRoomParameter::class.simpleName}") {
                cut.getCreateRoomParameter("bla").shouldBe(CreateRoomParameter())
            }
        }

        describe(DefaultAppserviceRoomService::onCreatedRoom.name) {
            it("should save room alias") {
                cut.onCreatedRoom("someRoomAlias", "someRoomId")
                coVerify { roomServiceMock.getOrCreateRoomAlias("someRoomAlias", "someRoomId") }
            }
        }
    }
}