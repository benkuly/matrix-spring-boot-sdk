package net.folivo.matrix.bot.appservice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier

class MatrixAppserviceServiceHelperTest {

    @Test
    fun `should allow user creation, when managed by this appservice`() {
        val cut = MatrixAppserviceServiceHelper(listOf("unicorn_.+"), listOf())
        StepVerifier.create(cut.isManagedUser("unicorn_fluffy"))
                .assertNext { assertThat(it).isTrue() }
                .verifyComplete()
    }

    @Test
    fun `should not allow user creation, when not managed by this appservice`() {
        val cut = MatrixAppserviceServiceHelper(listOf("unicorn_.+"), listOf())
        StepVerifier.create(cut.isManagedUser("dino_fluffy"))
                .assertNext { assertThat(it).isFalse() }
                .verifyComplete()
    }

    @Test
    fun `should allow room creation, when managed by this appservice`() {
        val cut = MatrixAppserviceServiceHelper(listOf(), listOf("unicorn_.+"))
        StepVerifier.create(cut.isManagedRoom("unicorn_fluffy"))
                .assertNext { assertThat(it).isTrue() }
                .verifyComplete()
    }

    @Test
    fun `should not allow room creation, when managed by this appservice`() {
        val cut = MatrixAppserviceServiceHelper(listOf(), listOf("unicorn_.+"))
        StepVerifier.create(cut.isManagedRoom("dino_fluffy"))
                .assertNext { assertThat(it).isFalse() }
                .verifyComplete()
    }
}