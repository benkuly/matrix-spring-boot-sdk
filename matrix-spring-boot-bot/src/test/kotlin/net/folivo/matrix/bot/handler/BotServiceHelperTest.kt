package net.folivo.matrix.bot.handler

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BotServiceHelperTest {

    @Test
    fun `should allow user creation, when managed by this appservice`() {
        val cut = BotServiceHelper(listOf("unicorn_.+"), listOf(), "bot")

        assertThat(runBlocking { cut.isManagedUser("@unicorn_fluffy:someServer") }).isTrue()
        assertThat(runBlocking { cut.isManagedUser("@bot:someServer") }).isTrue()
    }

    @Test
    fun `should not allow user creation, when not managed by this appservice`() {
        val cut = BotServiceHelper(listOf("unicorn_.+"), listOf(), "bot")

        assertThat(runBlocking { cut.isManagedUser("@dino_fluffy:someServer") }).isFalse()
    }

    @Test
    fun `should allow room creation, when managed by this appservice`() {
        val cut = BotServiceHelper(listOf(), listOf("unicorn_.+"), "bot")

        assertThat(runBlocking { cut.isManagedRoom("#unicorn_fluffy:someServer") }).isTrue()
    }

    @Test
    fun `should not allow room creation, when managed by this appservice`() {
        val cut = BotServiceHelper(listOf(), listOf("unicorn_.+"), "bot")

        assertThat(runBlocking { cut.isManagedRoom("#dino_fluffy:someServer") }).isFalse()
    }
}