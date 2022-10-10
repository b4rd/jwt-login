package com.example.jwtlogin.ui.screen

import com.example.jwtlogin.data.model.AuthenticationData
import com.example.jwtlogin.data.repository.AuthenticationDataRepository
import com.example.jwtlogin.data.service.AuthenticationService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    lateinit var authenticationService: AuthenticationService

    @MockK
    lateinit var authenticationDataRepository: AuthenticationDataRepository

    @InjectMockKs
    lateinit var loginViewModel: LoginViewModel

    @Before
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `initial ui state is correct`() {
        assertEquals(LoginUiState(false, null), loginViewModel.uiState.value)
    }

    @Test
    fun `loading is set to true when login is called`() {
        every {
            authenticationService.login(any(), any())
        } returns Single.never()

        loginViewModel.login("bond", "007")

        assertEquals(LoginUiState(true, null), loginViewModel.uiState.value)
    }

    @Test
    fun `unknownError is set when the login request fails with a general error`() {
        every {
            authenticationService.login(any(), any())
        } returns Single.error(Exception())

        loginViewModel.login("bond", "007")

        assertEquals(LoginUiState(false, LoginResult.Failure.UnknownError), loginViewModel.uiState.value)
    }

    @Test
    fun `invalidCredentials error is set when the login request fails with 401`() {
        val unauthorizedResponse = Response.error<String>(
            401,
            ResponseBody.create(MediaType.get("text/plain"), "Unauthorized")
        )
        every {
            authenticationService.login(any(), any())
        } returns Single.error(HttpException(unauthorizedResponse))

        loginViewModel.login("bond", "007")

        assertEquals(LoginUiState(false, LoginResult.Failure.InvalidCredentials), loginViewModel.uiState.value)
    }

    @Test
    fun `noInternet error is set when the login request fails with an UnknownHostException`() {
        every {
            authenticationService.login(any(), any())
        } returns Single.error(UnknownHostException())

        loginViewModel.login("bond", "007")

        assertEquals(LoginUiState(false, LoginResult.Failure.NoInternet), loginViewModel.uiState.value)
    }

    @Test
    fun `unknownError is set when an error occurs while persisting a token`() {
        val authenticationData = AuthenticationData("test token")
        every {
            authenticationService.login(any(), any())
        } returns Single.just(authenticationData)
        every {
            authenticationDataRepository.save(any())
        } returns Single.error(IOException())

        loginViewModel.login("bond", "007")

        assertEquals(LoginUiState(false, LoginResult.Failure.UnknownError), loginViewModel.uiState.value)

        verify { authenticationService.login("bond", "007") }
        verify { authenticationDataRepository.save(authenticationData) }
    }

    @Test
    fun `success flag is set when login completes and the token is persisted`() {
        val authenticationData = AuthenticationData("test token")
        every {
            authenticationService.login(any(), any())
        } returns Single.just(authenticationData)
        every {
            authenticationDataRepository.save(any())
        } returns Single.just(authenticationData)

        loginViewModel.login("bond", "007")

        assertEquals(LoginUiState(false, LoginResult.Success), loginViewModel.uiState.value)

        verify { authenticationService.login("bond", "007") }
        verify { authenticationDataRepository.save(authenticationData) }
    }

    @Test
    fun `login result is cleared when the error dialog is dismissed`() {
        every {
            authenticationService.login(any(), any())
        } returns Single.error(Exception())

        loginViewModel.login("bond", "007")

        assertEquals(LoginUiState(false, LoginResult.Failure.UnknownError), loginViewModel.uiState.value)

        loginViewModel.onErrorDialogDismissed()
        assertEquals(LoginUiState(false, null), loginViewModel.uiState.value)
    }
}