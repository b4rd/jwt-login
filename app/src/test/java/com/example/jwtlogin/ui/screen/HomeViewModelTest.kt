package com.example.jwtlogin.ui.screen

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.jwtlogin.data.model.AuthenticationData
import com.example.jwtlogin.data.repository.AuthenticationDataRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.reactivex.rxjava3.core.Flowable
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.*

class HomeViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    lateinit var authenticationDataRepository: AuthenticationDataRepository

    @InjectMockKs
    lateinit var homeViewModel: HomeViewModel

    @Test
    fun `there is no user info initially`() {
        assertEquals(HomeUiState(UserInfo("", "")), homeViewModel.uiState.value)
    }

    @Test
    fun `ui state is not updated when auth data cannot be fetched`() {
        every {
            authenticationDataRepository.fetch()
        } returns Flowable.error(IOException())

        homeViewModel.init()

        assertEquals(HomeUiState(UserInfo("", "")), homeViewModel.uiState.value)
    }

    @Test
    fun `ui state is not updated when there is no persisted auth data`() {
        every {
            authenticationDataRepository.fetch()
        } returns Flowable.just(Optional.empty())

        homeViewModel.init()

        assertEquals(HomeUiState(UserInfo("", "")), homeViewModel.uiState.value)
    }

    @Test
    fun `ui state is not updated when auth data is corrupted`() {
        every {
            authenticationDataRepository.fetch()
        } returns Flowable.just(Optional.of(AuthenticationData("xyz")))

        homeViewModel.init()

        assertEquals(HomeUiState(UserInfo("", "")), homeViewModel.uiState.value)
    }

    @Test
    fun `ui state contains user info from the token when valid authentication data is fetched`() {
        val userName = "James Bond"
        val role = "marksman"

        every {
            authenticationDataRepository.fetch()
        } returns Flowable.just(Optional.of(AuthenticationData(generateAccessToken(userName, role))))

        homeViewModel.init()

        assertEquals(HomeUiState(UserInfo(userName, role)), homeViewModel.uiState.value)
    }

    @Suppress("SameParameterValue")
    private fun generateAccessToken(userName: String, role: String): String {
        return JWT.create()
            .withClaim("idp:user_id", "1")
            .withClaim("idp:user_name", userName)
            .withClaim("idp:fullname", userName)
            .withClaim("role", role)
            .withClaim("exp", 10000)
            .sign(Algorithm.HMAC256("secret"))
    }
}