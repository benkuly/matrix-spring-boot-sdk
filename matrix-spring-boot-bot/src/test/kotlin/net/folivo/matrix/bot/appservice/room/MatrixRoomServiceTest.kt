package net.folivo.matrix.bot.appservice.room

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Mono

class MatrixRoomServiceTest : DescribeSpec(testBody())

private fun testBody(): DescribeSpec.() -> Unit {
    return {
        val roomRepositoryMock: MatrixRoomRepository = mockk(relaxed = true)
        val roomAliasRepositoryMock: MatrixRoomAliasRepository = mockk(relaxed = true)

        val cut = MatrixRoomService(
                roomRepositoryMock,
                roomAliasRepositoryMock,
        )

        describe(MatrixRoomService::getOrCreateRoom.name) {
            val room = MatrixRoom("someRoomId")

            it("should save new room when room does not exist") {
                every { roomRepositoryMock.findById(any<String>()) }.returns(Mono.empty())
                every { roomRepositoryMock.save(any()) }.returns(Mono.just(room))
                cut.getOrCreateRoom("someRoomId").shouldBe(room)
                verify { roomRepositoryMock.save(room) }
            }
            it("should use existing room") {
                every { roomRepositoryMock.findById(any<String>()) }.returns(Mono.just(room))
                cut.getOrCreateRoom("someRoomId").shouldBe(room)
                verify(exactly = 0) { roomRepositoryMock.save(any()) }
            }
        }

        describe(MatrixRoomService::getOrCreateRoomAlias.name) {
            val roomAlias = MatrixRoomAlias("someAlias", "someRoomId")

            it("should save new room alias when it does not exist") {
                every { roomAliasRepositoryMock.findById(any<String>()) }.returns(Mono.empty())
                every { roomAliasRepositoryMock.save(any()) }.returns(Mono.just(roomAlias))
                cut.getOrCreateRoomAlias("someRoomAlias", "someRoomId").shouldBe(roomAlias)
                verify { roomAliasRepositoryMock.save(roomAlias) }
            }
            it("should use existing room alias") {
                every { roomAliasRepositoryMock.findById(any<String>()) }.returns(Mono.just(roomAlias))
                cut.getOrCreateRoomAlias("someRoomAlias", "someRoomId").shouldBe(roomAlias)
                verify(exactly = 0) { roomAliasRepositoryMock.save(roomAlias) }
            }
            it("should change existing room alias") {
                val oldRoomAlias = MatrixRoomAlias("someAlias", "someOldRoomId")
                every { roomAliasRepositoryMock.findById(any<String>()) }.returns(Mono.just(oldRoomAlias))
                every { roomAliasRepositoryMock.save(any()) }.returns(Mono.just(roomAlias))
                cut.getOrCreateRoomAlias("someRoomAlias", "someRoomId").shouldBe(roomAlias)
                verify { roomAliasRepositoryMock.save(roomAlias) }
            }
        }
    }
}