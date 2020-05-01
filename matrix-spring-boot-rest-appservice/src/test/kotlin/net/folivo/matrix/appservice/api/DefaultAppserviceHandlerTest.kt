package net.folivo.matrix.appservice.api

import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.rooms.Visibility
import net.folivo.matrix.restclient.api.user.RegisterResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockKExtension::class)
class DefaultAppserviceHandlerTest {

    @MockK
    lateinit var matrixClientMock: MatrixClient

    @MockK
    lateinit var matrixAppserviceUserServiceMock: MatrixAppserviceUserService

    @MockK
    lateinit var matrixAppserviceRoomrServiceMock: MatrixAppserviceRoomService

    @InjectMockKs
    lateinit var cut: DefaultAppserviceHandler

    @Test
    fun `should hasUser when delegated service says it exists`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(MatrixAppserviceUserService.UserExistingState.EXISTS)

        val result = cut.hasUser("@someUserId:example.com").block()
        assertThat(result).isTrue()
    }

    @Test
    fun `should hasUser and create it when delegated service want to`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(MatrixAppserviceUserService.UserExistingState.CAN_BE_CREATED)
        every { matrixAppserviceUserServiceMock.saveUser("@someUserId:example.com") } returns Unit

        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.just(RegisterResponse("@someUserId:example.com"))

        val result = cut.hasUser("@someUserId:example.com").block()
        assertThat(result).isTrue()

        verify {
            matrixClientMock.userApi.register(
                    authenticationType = "m.login.application_service",
                    username = "someUserId"
            )
        }
        verify { matrixAppserviceUserServiceMock.saveUser("@someUserId:example.com") }
    }

    @Test
    fun `should not hasUser when register fails`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(MatrixAppserviceUserService.UserExistingState.CAN_BE_CREATED)

        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.error(
                MatrixServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorResponse("500", "M_UNKNOWN")
                )
        )

        val result = cut.hasUser("@someUserId:example.com")
        StepVerifier.create(result)
                .verifyError()

        verify(exactly = 0) { matrixAppserviceUserServiceMock.saveUser(any()) }
    }

    @Test
    fun `should not hasUser when saving by service fails`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(MatrixAppserviceUserService.UserExistingState.CAN_BE_CREATED)
        every { matrixAppserviceUserServiceMock.saveUser("@someUserId:example.com") } throws RuntimeException()

        every {
            matrixClientMock.userApi.register(allAny())
        } returns Mono.just(RegisterResponse("@someUserId:example.com"))

        val result = cut.hasUser("@someUserId:example.com")
        StepVerifier.create(result)
                .verifyError()
    }

    @Test
    fun `should not hasUser when delegated service says it does not exists and should not be created`() {
        every { matrixAppserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(MatrixAppserviceUserService.UserExistingState.DOES_NOT_EXISTS)

        val result = cut.hasUser("@someUserId:example.com").block()
        assertThat(result).isFalse()

        verify(exactly = 0) { matrixClientMock.userApi wasNot Called }
        verify(exactly = 0) { matrixAppserviceUserServiceMock.saveUser(any()) }
    }

    @Test
    fun `should hasRoomAlias when delegated service says it exists`() {
        every { matrixAppserviceRoomrServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(MatrixAppserviceRoomService.RoomExistingState.EXISTS)

        val result = cut.hasRoomAlias("#someRoomAlias:example.com").block()
        assertThat(result).isTrue()
    }

    @Test
    fun `should hasRoomAlias and create it when delegated service want to`() {
        every { matrixAppserviceRoomrServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(MatrixAppserviceRoomService.RoomExistingState.CAN_BE_CREATED)
        every { matrixAppserviceRoomrServiceMock.saveRoom("#someRoomAlias:example.com", "someRoomId") } returns Unit
        every { matrixAppserviceRoomrServiceMock.getCreateRoomParameter("#someRoomAlias:example.com") }
                .returns(CreateRoomParameter(name = "someName"))

        every {
            matrixClientMock.roomsApi.createRoom(allAny())
        } returns Mono.just("someRoomId")

        val result = cut.hasRoomAlias("#someRoomAlias:example.com").block()
        assertThat(result).isTrue()

        verify {
            matrixClientMock.roomsApi.createRoom(
                    roomAliasName = "someRoomAlias",
                    visibility = Visibility.PUBLIC,
                    name = "someName"
            )
        }
        verify { matrixAppserviceRoomrServiceMock.saveRoom("#someRoomAlias:example.com", "someRoomId") }
    }

    @Test
    fun `should not hasRoomAlias when creation fails`() {
        every { matrixAppserviceRoomrServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(MatrixAppserviceRoomService.RoomExistingState.CAN_BE_CREATED)
        every { matrixAppserviceRoomrServiceMock.getCreateRoomParameter("#someRoomAlias:example.com") }
                .returns(CreateRoomParameter(name = "someName"))

        every {
            matrixClientMock.roomsApi.createRoom(allAny())
        } returns Mono.error(
                MatrixServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorResponse("500", "M_UNKNOWN")
                )
        )

        val result = cut.hasRoomAlias("#someRoomAlias:example.com")
        StepVerifier.create(result)
                .verifyError()

        verify(exactly = 0) { matrixAppserviceUserServiceMock.saveUser(any()) }
    }

    @Test
    fun `should not hasRoomAlias when saving by service fails`() {
        every { matrixAppserviceRoomrServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(MatrixAppserviceRoomService.RoomExistingState.CAN_BE_CREATED)
        every { matrixAppserviceRoomrServiceMock.getCreateRoomParameter("#someRoomAlias:example.com") }
                .returns(CreateRoomParameter(name = "someName"))
        every {
            matrixAppserviceRoomrServiceMock.saveRoom(
                    "#someRoomAlias:example.com",
                    "someRoomId"
            )
        } throws RuntimeException()

        every {
            matrixClientMock.roomsApi.createRoom(allAny())
        } returns Mono.just("someRoomId")

        val result = cut.hasRoomAlias("#someRoomAlias:example.com")
        StepVerifier.create(result)
                .verifyError()
    }

    @Test
    fun `should not hasRoomAlias when delegated service says it does not exists and should not be created`() {
        every { matrixAppserviceRoomrServiceMock.roomExistingState("#someRoomAlias:example.com") }
                .returns(MatrixAppserviceRoomService.RoomExistingState.DOES_NOT_EXISTS)

        val result = cut.hasRoomAlias("#someRoomAlias:example.com").block()
        assertThat(result).isFalse()

        verify(exactly = 0) { matrixClientMock.roomsApi wasNot Called }
        verify(exactly = 0) { matrixAppserviceUserServiceMock.saveUser(any()) }
    }
}