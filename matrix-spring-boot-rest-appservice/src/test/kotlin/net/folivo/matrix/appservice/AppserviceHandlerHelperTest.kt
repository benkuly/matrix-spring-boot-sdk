package net.folivo.matrix.appservice

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import kotlinx.coroutines.runBlocking
import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.appservice.api.room.AppserviceRoomService
import net.folivo.matrix.appservice.api.room.CreateRoomParameter
import net.folivo.matrix.appservice.api.user.AppserviceUserService
import net.folivo.matrix.appservice.api.user.AppserviceUserService.UserExistingState
import net.folivo.matrix.appservice.api.user.RegisterUserParameter
import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.rooms.Visibility
import net.folivo.matrix.restclient.api.user.RegisterResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR

@ExtendWith(MockKExtension::class)
class AppserviceHandlerHelperTest {

    @MockK
    lateinit var matrixClientMock: MatrixClient

    @MockK
    lateinit var appserviceUserServiceMock: AppserviceUserService

    @MockK
    lateinit var appserviceRoomServiceMock: AppserviceRoomService

    @InjectMockKs
    lateinit var cut: AppserviceHandlerHelper

    @BeforeEach
    fun beforeEach() {
        coEvery { appserviceUserServiceMock.userExistingState(allAny()) }
                .returns(UserExistingState.CAN_BE_CREATED)
        coEvery { appserviceUserServiceMock.getRegisterUserParameter(allAny()) }
                .returns(RegisterUserParameter())
        coEvery { appserviceUserServiceMock.onRegisteredUser(allAny()) } just Runs
        coEvery { appserviceRoomServiceMock.onCreatedRoom(any(), any()) } just Runs
        coEvery { appserviceRoomServiceMock.getCreateRoomParameter(any()) }
                .returns(CreateRoomParameter())
    }

    @Test
    fun `should create and save user`() {
        coEvery { appserviceUserServiceMock.getRegisterUserParameter("@someUserId:example.com") }
                .returns(RegisterUserParameter("someDisplayName"))
        coEvery { matrixClientMock.userApi.register(allAny()) }
                .returns(RegisterResponse("@someUserId:example.com"))
        coEvery { matrixClientMock.userApi.setDisplayName(allAny()) } just Runs

        runBlocking { cut.registerManagedUser("@someUserId:example.com") }

        coVerify {
            matrixClientMock.userApi.register(
                    authenticationType = "m.login.application_service",
                    username = "someUserId"
            )
            matrixClientMock.userApi.setDisplayName(
                    "@someUserId:example.com",
                    "someDisplayName",
                    "@someUserId:example.com"
            )
            appserviceUserServiceMock.onRegisteredUser("@someUserId:example.com")
        }
    }

    @Test
    fun `should have error when register fails`() {
        coEvery { appserviceUserServiceMock.userExistingState("@someUserId:example.com") }
                .returns(UserExistingState.CAN_BE_CREATED)

        coEvery { matrixClientMock.userApi.register(allAny()) }
                .throws(
                        MatrixServerException(
                                INTERNAL_SERVER_ERROR,
                                ErrorResponse("500", "M_UNKNOWN")
                        )
                )

        try {
            runBlocking { cut.registerManagedUser("@someUserId:example.com") }
            fail { "should have error" }
        } catch (error: Throwable) {

        }

        coVerify(exactly = 0) { appserviceUserServiceMock.onRegisteredUser(any()) }
    }

    @Test
    fun `should catch error when register fails due to already existing id`() {
        coEvery { appserviceUserServiceMock.getRegisterUserParameter("@someUserId:example.com") }
                .returns(RegisterUserParameter("someDisplayName"))
        coEvery { matrixClientMock.userApi.register(allAny()) }
                .returns(RegisterResponse("@someUserId:example.com"))
        coEvery { matrixClientMock.userApi.setDisplayName(allAny()) } just Runs

        coEvery { matrixClientMock.userApi.register(allAny()) }
                .throws(
                        MatrixServerException(
                                BAD_REQUEST,
                                ErrorResponse("M_USER_IN_USE", "Desired user ID is already taken.")
                        )
                )

        runBlocking {
            cut.registerManagedUser("@someUserId:example.com")
        }

        coVerify {
            matrixClientMock.userApi.register(
                    authenticationType = "m.login.application_service",
                    username = "someUserId"
            )
            matrixClientMock.userApi.setDisplayName(
                    "@someUserId:example.com",
                    "someDisplayName",
                    "@someUserId:example.com"
            )
            appserviceUserServiceMock.onRegisteredUser("@someUserId:example.com")
        }
    }

    @Test
    fun `should have error when saving by user service fails`() {
        coEvery { appserviceUserServiceMock.onRegisteredUser("@someUserId:example.com") }
                .throws(RuntimeException())
        coEvery { appserviceUserServiceMock.getRegisterUserParameter("@someUserId:example.com") }
                .returns(RegisterUserParameter(displayName = "someDisplayName"))

        coEvery { matrixClientMock.userApi.setDisplayName(allAny()) } just Runs
        coEvery { matrixClientMock.userApi.register(allAny()) }
                .returns(RegisterResponse("@someUserId:example.com"))

        try {
            runBlocking { cut.registerManagedUser("@someUserId:example.com") }
            fail { "should have error" }
        } catch (error: Throwable) {
        }

        coVerify {
            matrixClientMock.userApi.setDisplayName(
                    "@someUserId:example.com",
                    displayName = "someDisplayName",
                    asUserId = "@someUserId:example.com"
            )
        }
    }

    @Test
    fun `should not set displayName if null`() {
        coEvery { appserviceUserServiceMock.getRegisterUserParameter("@someUserId:example.com") }
                .returns(RegisterUserParameter())
        coEvery { matrixClientMock.userApi.register(allAny()) }
                .returns(RegisterResponse("@someUserId:example.com"))

        runBlocking { cut.registerManagedUser("@someUserId:example.com") }

        val userApi = matrixClientMock.userApi
        coVerify(exactly = 0) { userApi.setDisplayName(allAny()) }
    }

    @Test
    fun `should not have error when setting displayName fails`() {
        coEvery { matrixClientMock.userApi.setDisplayName(allAny()) }
                .throws(MatrixServerException(BAD_REQUEST, ErrorResponse("M_UNKNOWN")))
        coEvery { matrixClientMock.userApi.register(allAny()) }
                .returns(RegisterResponse("@someUserId:example.com"))

        runBlocking { cut.registerManagedUser("@someUserId:example.com") }
    }

    @Test
    fun `should create and save room`() {
        coEvery { appserviceRoomServiceMock.getCreateRoomParameter("#someRoomAlias:example.com") }
                .returns(CreateRoomParameter(name = "someName"))

        coEvery { matrixClientMock.roomsApi.createRoom(allAny()) }
                .returns("someRoomId")

        runBlocking { cut.createManagedRoom("#someRoomAlias:example.com") }

        coVerify {
            matrixClientMock.roomsApi.createRoom(
                    roomAliasName = "someRoomAlias",
                    visibility = Visibility.PUBLIC,
                    name = "someName"
            )
            appserviceRoomServiceMock.onCreatedRoom("#someRoomAlias:example.com", any())
        }
    }

    @Test
    fun `should have error when creation fails`() {
        coEvery { matrixClientMock.roomsApi.createRoom(allAny()) }
                .throws(
                        MatrixServerException(
                                INTERNAL_SERVER_ERROR,
                                ErrorResponse("500", "M_UNKNOWN")
                        )
                )

        try {
            runBlocking { cut.createManagedRoom("#someRoomAlias:example.com") }
            fail { "should have error" }
        } catch (error: Throwable) {

        }

        coVerify(exactly = 0) { appserviceRoomServiceMock.onCreatedRoom(any(), any()) }
    }

    @Test
    fun `should have error when saving by room service fails`() {
        coEvery { appserviceRoomServiceMock.onCreatedRoom("#someRoomAlias:example.com", any()) }
                .throws(RuntimeException())

        coEvery { matrixClientMock.roomsApi.createRoom(allAny()) }
                .returns("someRoomId")

        try {
            runBlocking { cut.createManagedRoom("#someRoomAlias:example.com") }
            fail { "should have error" }
        } catch (error: Throwable) {
        }

        coVerify { appserviceRoomServiceMock.onCreatedRoom(any(), any()) }
    }

}