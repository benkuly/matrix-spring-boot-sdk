package net.folivo.matrix.core.config

import net.folivo.matrix.core.jackson.MatrixEventJacksonModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner


class MatrixAutoconfigurationContextTest {

    private val contextRunner = ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(
                            MatrixAutoconfiguration::class.java
                    )
            )

    @Test
    fun `default services`() {
        this.contextRunner
                .run { context: AssertableApplicationContext ->
                    assertThat(context).hasSingleBean(DefaultMatrixConfigurer::class.java)
                    assertThat(context).hasSingleBean(MatrixConfiguration::class.java)
                    assertThat(context).hasSingleBean(MatrixEventJacksonModule::class.java)
                }
    }
}