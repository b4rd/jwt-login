package com.example.jwtlogin.ui.screen

import com.example.jwtlogin.data.model.AuthenticationData
import com.example.jwtlogin.data.repository.AuthenticationDataRepository
import com.example.jwtlogin.data.service.AuthenticationService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class EntryViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    lateinit var authenticationService: AuthenticationService

    @MockK
    lateinit var authenticationDataRepository: AuthenticationDataRepository

    @InjectMockKs
    lateinit var entryViewModel: EntryViewModel

    @Test
    fun `refreshAuthenticationResult is null initially`() {
        assertEquals(EntryUiState( null), entryViewModel.uiState.value)
    }

    @Test
    fun `failure result is set when no authentication data is persisted yet`() {
        every {
            authenticationDataRepository.fetch()
        } returns Flowable.just(Optional.empty())

        entryViewModel.tryRefreshToken()

        assertEquals(EntryUiState(RefreshAuthenticationResult.Failure), entryViewModel.uiState.value)
    }

    @Test
    fun `failure result is set when the refresh token request fails`() {
        every {
            authenticationDataRepository.fetch()
        } returns Flowable.just(Optional.of(AuthenticationData("xyz")))
        every {
            authenticationService.refreshToken(any())
        } returns Single.error(IOException())

        entryViewModel.tryRefreshToken()

        assertEquals(EntryUiState(RefreshAuthenticationResult.Failure), entryViewModel.uiState.value)
    }

    @Test
    fun `failure result is set when the new authentication data cannot be persisted`() {
        every {
            authenticationDataRepository.fetch()
        } returns Flowable.just(Optional.of(AuthenticationData("xyz")))
        every {
            authenticationService.refreshToken(any())
        } returns Single.just(AuthenticationData("abc"))
        every {
            authenticationDataRepository.save(any())
        } returns Single.error(IOException())

        entryViewModel.tryRefreshToken()

        assertEquals(EntryUiState(RefreshAuthenticationResult.Failure), entryViewModel.uiState.value)
    }

    @Test
    fun `success result is set when the new token is received and persisted`() {
        val oldAuthData = AuthenticationData(accessToken =  "xyz", refreshToken = "refresh")
        val newAuthData = AuthenticationData("abc")
        every {
            authenticationDataRepository.fetch()
        } returns Flowable.just(Optional.of(oldAuthData))
        every {
            authenticationService.refreshToken(any())
        } returns Single.just(newAuthData)
        every {
            authenticationDataRepository.save(any())
        } returns Single.just(newAuthData)

        entryViewModel.tryRefreshToken()

        assertEquals(EntryUiState(RefreshAuthenticationResult.Success), entryViewModel.uiState.value)
        verify { authenticationService.refreshToken(oldAuthData.refreshToken) }
        verify { authenticationDataRepository.save(newAuthData) }
    }
}