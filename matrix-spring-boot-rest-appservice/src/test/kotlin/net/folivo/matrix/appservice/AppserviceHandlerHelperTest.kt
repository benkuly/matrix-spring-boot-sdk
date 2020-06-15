package net.folivo.matrix.appservice

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.room.MatrixAppserviceRoomService
import net.folivo.matrix.appservice.api.user.CreateUserParameter
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService
import net.folivo.matrix.appservice.api.user.MatrixAppserviceUserService.UserExistingState
import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.rooms.Visibility
import net.folivo.matrix.restclient.api.user.RegisterResponse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockKExtension::class)
class AppserviceHandlerHelperTest {

    @MockK
    lateinit var matrixClientMock: MatrixClient

    @MockK
    lateinit var matrixAppserviceUserServiceMock: MatrixAppserviceUserService

    @MockK
    lateinit var matrixAppserviceRoomServiceMock: MatrixAppserviceRoomService

    @InjectMockKs
    lateinit var cut: AppserviceHandlerHelper

    @BeforeEach
    fun beforeEach() {
        every { matrixAppserviceUserServiceMock.userExistingState(allAny()) }
                .returns(Mono.just(UserExistingState.CAN_BE_CREATED))
        every { matrixAppserviceUserServiceMock.getCreateUserParameter(allAny()) }
                .returns(Mono.just(CreateUserParameter()))
        every { matrixAppserviceUserServiceMock.saveUser(allAny()) }
                .returns(Mono.empty())
        every { matrixAppserviceRoomServiceMock.saveRoom(any(), any()) }
                .returns(Mono.empty())
        every { matrixAppserviceRoomServiceMock.getCreateRoomParameter(any()) }
                .returns(Mono.just(CreateRoomParameter()))
    }

    @Test
    fun `should create and save user`() {
        every { matrixAppserviceUserServiceMock.getCreateUserParameter("@someUserId:example.com") }
                .returns(Mono.just(CreateUserParameter("someDisplayName")))
        every { matrixClientMock.userApi.register(allAny()) }
                .returns(Mono.just(RegisterResponse("@someUserId:example.com")))
        every { matrixClientMock.userApi.setDisplayName(allAny()) }
                .returns(Mono.empty())

        StepVerifier
                .create(cut.registerAndSaveUser("@someUserId:example.com"))
                .assertNext { Assertions.assertThat(it).isTrue() }
                .verifyComplete()

        verify {
            matrixClientMock.userApi.register(
                    authenticationType = "m.login.application_service",
                    username = "someUserId"
            )
            matrixClientMock.userApi.setDisplayName(
                    "@someUserId:example.com",
                    "someDisplayName",
                    "@someUserId:example.com"
            )
            matrixAppserviceUserServiceMock.saveUser("@someUserId:example.com")
        }
    }

    @Test
    fun `should have error when register fails`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(Mono.just(UserExistingState.CAN_BE_CREATED))

        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.error(
                MatrixServerException(
                        INTERNAL_SERVER_ERROR,
                        ErrorResponse("500", "M_UNKNOWN")
                )
        )

        StepVerifier
                .create(cut.registerAndSaveUser("@someUserId:example.com"))
                .verifyError()

        verify(exactly = 0) { matrixAppserviceUserServiceMock.saveUser(any()) }
    }

    @Test
    fun `should catch error when register fails due to already existing id`() {
        every { matrixAppserviceUserServiceMock.getCreateUserParameter("@someUserId:example.com") }
                .returns(Mono.just(CreateUserParameter("someDisplayName")))
        every { matrixClientMock.userApi.register(allAny()) }
                .returns(Mono.just(RegisterResponse("@someUserId:example.com")))
        every { matrixClientMock.userApi.setDisplayName(allAny()) }
                .returns(Mono.empty())

        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.error(
                MatrixServerException(
                        BAD_REQUEST,
                        ErrorResponse("M_USER_IN_USE", "Desired user ID is already taken.")
                )
        )

        StepVerifier
                .create(cut.registerAndSaveUser("@someUserId:example.com"))
                .assertNext { Assertions.assertThat(it).isTrue() }
                .verifyComplete()

        verify {
            matrixClientMock.userApi.register(
                    authenticationType = "m.login.application_service",
                    username = "someUserId"
            )
            matrixClientMock.userApi.setDisplayName(
                    "@someUserId:example.com",
                    "someDisplayName",
                    "@someUserId:example.com"
            )
            matrixAppserviceUserServiceMock.saveUser("@someUserId:example.com")
        }
    }

    @Test
    fun `should not has error when saving by user service fails`() {
        every { matrixAppserviceUserServiceMock.saveUser("@someUserId:example.com") }
                .returns(Mono.error(RuntimeException()))
        every { matrixAppserviceUserServiceMock.getCreateUserParameter("@someUserId:example.com") }
                .returns(Mono.just(CreateUserParameter(displayName = "someDisplayName")))

        every {
            matrixClientMock.userApi.setDisplayName(allAny())
        } returns Mono.empty()
        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.just(RegisterResponse("@someUserId:example.com"))

        StepVerifier
                .create(cut.registerAndSaveUser("@someUserId:example.com"))
                .assertNext { Assertions.assertThat(it).isTrue() }
                .verifyComplete()

        verify {
            matrixClientMock.userApi.setDisplayName(
                    "@someUserId:example.com",
                    displayName = "someDisplayName",
                    asUserId = "@someUserId:example.com"
            )
        }
    }

    @Test
    fun `should not set displayName if null`() {
        every { matrixAppserviceUserServiceMock.getCreateUserParameter("@someUserId:example.com") }
                .returns(Mono.just(CreateUserParameter()))
        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.just(RegisterResponse("@someUserId:example.com"))

        StepVerifier
                .create(cut.registerAndSaveUser("@someUserId:example.com"))
                .assertNext { Assertions.assertThat(it).isTrue() }
                .verifyComplete()

        val userApi = matrixClientMock.userApi
        verify(exactly = 0) { userApi.setDisplayName(allAny()) }
    }

    @Test
    fun `should not has error when setting displayName fails`() {
        every {
            matrixClientMock.userApi.setDisplayName(allAny())
        } returns Mono.error(MatrixServerException(HttpStatus.BAD_REQUEST, ErrorResponse("M_UNKNOWN")))
        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.just(RegisterResponse("@someUserId:example.com"))

        StepVerifier
                .create(cut.registerAndSaveUser("@someUserId:example.com"))
                .assertNext { Assertions.assertThat(it).isTrue() }
                .verifyComplete()
    }

    @Test
    fun `should create and save room`() {
        every { matrixAppserviceRoomServiceMock.getCreateRoomParameter("#someRoomAlias:example.com") }
                .returns(Mono.just(CreateRoomParameter(name = "someName")))

        every {
            matrixClientMock.roomsApi.createRoom(allAny())
        } returns Mono.just("someRoomId")

        StepVerifier
                .create(cut.createAndSaveRoom("#someRoomAlias:example.com"))
                .assertNext { Assertions.assertThat(it).isTrue() }
                .verifyComplete()

        verify {
            matrixClientMock.roomsApi.createRoom(
                    roomAliasName = "someRoomAlias",
                    visibility = Visibility.PUBLIC,
                    name = "someName"
            )
        }
        verify { matrixAppserviceRoomServiceMock.saveRoom("#someRoomAlias:example.com", any()) }
    }

    @Test
    fun `should have error when creation fails`() {
        every {
            matrixClientMock.roomsApi.createRoom(allAny())
        } returns Mono.error(
                MatrixServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorResponse("500", "M_UNKNOWN")
                )
        )

        StepVerifier
                .create(cut.createAndSaveRoom("#someRoomAlias:example.com"))
                .verifyError()

        verify(exactly = 0) { matrixAppserviceRoomServiceMock.saveRoom(any(), any()) }
    }

    @Test
    fun `should not has error when saving by room service fails`() {
        every {
            matrixAppserviceRoomServiceMock.saveRoom(
                    "#someRoomAlias:example.com",
                    any()
            )
        }.returns(Mono.error(RuntimeException()))

        every {
            matrixClientMock.roomsApi.createRoom(allAny())
        } returns Mono.just("someRoomId")

        StepVerifier
                .create(cut.createAndSaveRoom("#someRoomAlias:example.com"))
                .assertNext { Assertions.assertThat(it).isTrue() }
                .verifyComplete()

        verify { matrixAppserviceRoomServiceMock.saveRoom(any(), any()) }
    }

}