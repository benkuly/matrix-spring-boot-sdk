package net.folivo.spring.matrix.restclient

import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.client.rest.api.sync.SyncBatchTokenService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner


class MatrixClientAutoconfigurationContextTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                MatrixClientAutoconfiguration::class.java,
            )
        )

    @Test
    fun `default services`() {
        this.contextRunner
            .withPropertyValues("matrix.client.homeServer.hostname=localhost", "matrix.client.token=test")
            .run { context: AssertableApplicationContext ->
                assertThat(context).hasSingleBean(MatrixClient::class.java)
                assertThat(context).hasSingleBean(SyncBatchTokenService::class.java)
            }
    }
}