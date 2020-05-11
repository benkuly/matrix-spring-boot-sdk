package net.folivo.matrix.bot.appservice.user

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import net.folivo.matrix.appservice.api.user.CreateUserParameter
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService.UserExistingState.*
import net.folivo.matrix.bot.appservice.AppserviceBotManager
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockKExtension::class)
class DefaultMatrixAppserviceUserServiceTest {
    @MockK
    lateinit var appserviceBotManagerMock: AppserviceBotManager

    @MockK
    lateinit var appserviceUserRepositoryMock: AppserviceUserRepository

    @InjectMockKs
    lateinit var cut: DefaultMatrixAppserviceUserService

    @Test
    fun `userExistingState should be EXISTS when user is in database`() {
        every { appserviceUserRepositoryMock.existsById("someUserId") }
                .returns(Mono.just(true))

        StepVerifier
                .create(cut.userExistingState("someUserId"))
                .assertNext { Assertions.assertThat(it).isEqualTo(EXISTS) }
                .verifyComplete()
    }

    @Test
    fun `userExistingState should be CAN_BE_CREATED when creation is allowed`() {
        every { appserviceUserRepositoryMock.existsById("someUserId") }
                .returns(Mono.just(false))

        every { appserviceBotManagerMock.shouldCreateUser("someUserId") }
                .returns(Mono.just(true))

        StepVerifier
                .create(cut.userExistingState("someUserId"))
                .assertNext { Assertions.assertThat(it).isEqualTo(CAN_BE_CREATED) }
                .verifyComplete()
    }

    @Test
    fun `userExistingState should be DOES_NOT_EXISTS when creation is not allowed`() {
        every { appserviceUserRepositoryMock.existsById("someUserId") }
                .returns(Mono.just(false))

        every { appserviceBotManagerMock.shouldCreateUser("someUserId") }
                .returns(Mono.just(false))

        StepVerifier
                .create(cut.userExistingState("someUserId"))
                .assertNext { Assertions.assertThat(it).isEqualTo(DOES_NOT_EXISTS) }
                .verifyComplete()
    }

    @Test
    fun `getCreateUserParameter should be delegated`() {
        every { appserviceBotManagerMock.getCreateUserParameter("someUserId") }
                .returns(Mono.just(CreateUserParameter()))

        StepVerifier
                .create(cut.getCreateUserParameter("someUserId"))
                .assertNext { Assertions.assertThat(it).isEqualTo(CreateUserParameter()) }
                .verifyComplete()

        verify { appserviceBotManagerMock.getCreateUserParameter("someUserId") }
    }

    @Test
    fun `should save user in database`() {
        val user = AppserviceUser("someUserId")
        every { appserviceUserRepositoryMock.save<AppserviceUser>(any()) }
                .returns(Mono.just(user))

        StepVerifier
                .create(cut.saveUser("someUserId"))
                .verifyComplete()

        verify { appserviceUserRepositoryMock.save(user) }
    }
}