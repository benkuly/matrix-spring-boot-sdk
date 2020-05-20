package net.folivo.matrix.bot.appservice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier

class DefaultAppserviceBotManagerTest {

    @Test
    fun `should allow user creation, when managed by this appservice`() {
        val cut = DefaultAppserviceBotManager(listOf("unicorn_.+"), listOf())
        StepVerifier.create(cut.shouldCreateUser("unicorn_fluffy"))
                .assertNext { assertThat(it).isTrue() }
                .verifyComplete()
    }

    @Test
    fun `should not allow user creation, when not managed by this appservice`() {
        val cut = DefaultAppserviceBotManager(listOf("unicorn_.+"), listOf())
        StepVerifier.create(cut.shouldCreateUser("dino_fluffy"))
                .assertNext { assertThat(it).isFalse() }
                .verifyComplete()
    }

    @Test
    fun `should allow room creation, when managed by this appservice`() {
        val cut = DefaultAppserviceBotManager(listOf(), listOf("unicorn_.+"))
        StepVerifier.create(cut.shouldCreateRoom("unicorn_fluffy"))
                .assertNext { assertThat(it).isTrue() }
                .verifyComplete()
    }

    @Test
    fun `should not allow room creation, when managed by this appservice`() {
        val cut = DefaultAppserviceBotManager(listOf(), listOf("unicorn_.+"))
        StepVerifier.create(cut.shouldCreateRoom("dino_fluffy"))
                .assertNext { assertThat(it).isFalse() }
                .verifyComplete()
    }
}