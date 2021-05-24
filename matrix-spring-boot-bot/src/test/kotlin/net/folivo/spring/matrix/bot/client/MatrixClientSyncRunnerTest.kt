package net.folivo.spring.matrix.bot.client

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.mockk
import net.folivo.trixnity.client.rest.MatrixClient

class MatrixClientSyncRunnerTest : DescribeSpec({
    val matrixClientMock: MatrixClient = mockk(relaxed = true)

    val cut = MatrixClientSyncRunner(matrixClientMock)

    describe(MatrixClientSyncRunner::startClientJob.name) {
        it("should start sync") {
            cut.startClientJob()
            coVerify { matrixClientMock.sync.start(wait = true) }
            cut.destroy()
        }
    }

    describe(MatrixClientSyncRunner::destroy.name) {
        it("should stop sync") {
            cut.destroy()
            coVerify {
                matrixClientMock.sync.stop()
            }
        }
    }

    afterTest { clearMocks(matrixClientMock) }
})