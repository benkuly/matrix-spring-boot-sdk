package net.folivo.matrix.bot.appservice.room

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class MatrixRoomServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val roomRepositoryMock: MatrixRoomRepository = mockk(relaxed = true)
        val roomAliasRepositoryMock: MatrixRoomAliasRepository = mockk(relaxed = true)

        val cut = MatrixRoomService(
                roomRepositoryMock,
                roomAliasRepositoryMock
        )

        describe(MatrixRoomService::getOrCreateRoom.name) {
            val room = MatrixRoom("someRoomId")

            it("should save new room when room does not exist") {
                coEvery { roomRepositoryMock.findById(any()) }.returns(null)
                coEvery { roomRepositoryMock.save(any()) }.returns(room)
                cut.getOrCreateRoom("someRoomId").shouldBe(room)
                coVerify { roomRepositoryMock.save(room) }
            }
            it("should use existing room") {
                coEvery { roomRepositoryMock.findById(any()) }.returns(room)
                cut.getOrCreateRoom("someRoomId").shouldBe(room)
                coVerify(exactly = 0) { roomRepositoryMock.save(any()) }
            }
        }

        describe(MatrixRoomService::getOrCreateRoomAlias.name) {
            val roomAlias = MatrixRoomAlias("someRoomAlias", "someRoomId")

            beforeTest {
                coEvery { roomRepositoryMock.findById(any()) }.returns(mockk())
            }
            it("should save new room alias when it does not exist") {
                coEvery { roomAliasRepositoryMock.findById(any()) }.returns(null)
                coEvery { roomAliasRepositoryMock.save(any()) }.returns(roomAlias)
                cut.getOrCreateRoomAlias("someRoomAlias", "someRoomId").shouldBe(roomAlias)
                coVerify { roomAliasRepositoryMock.save(roomAlias) }
            }
            it("should use existing room alias") {
                coEvery { roomAliasRepositoryMock.findById(any()) }.returns(roomAlias)
                cut.getOrCreateRoomAlias("someRoomAlias", "someRoomId").shouldBe(roomAlias)
                coVerify(exactly = 0) { roomAliasRepositoryMock.save(roomAlias) }
            }
            it("should change existing room alias") {
                val oldRoomAlias = MatrixRoomAlias("someRoomAlias", "someOldRoomId")
                coEvery { roomAliasRepositoryMock.findById(any()) }.returns(oldRoomAlias)
                coEvery { roomAliasRepositoryMock.save(any()) }.returns(roomAlias)
                cut.getOrCreateRoomAlias("someRoomAlias", "someRoomId").shouldBe(roomAlias)
                coVerify { roomAliasRepositoryMock.save(roomAlias) }
            }
        }

        afterTest {
            clearMocks(roomRepositoryMock, roomAliasRepositoryMock)
        }
    }
}