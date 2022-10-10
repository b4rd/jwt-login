package com.example.jwtlogin.ui.screen

import androidx.lifecycle.ViewModel
import com.example.jwtlogin.data.model.AuthenticationData
import com.example.jwtlogin.data.repository.AuthenticationDataRepository
import com.example.jwtlogin.data.service.AuthenticationService
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val authenticationService: AuthenticationService,
    private val authenticationDataRepository: AuthenticationDataRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(EntryUiState())
    private val compositeDisposable = CompositeDisposable()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun tryRefreshToken() {
        authenticationDataRepository.fetch().first(Optional.empty())
            .flatMap { optionalJwtToken -> refreshTokenOrThrow(optionalJwtToken) }
            .flatMap { refreshedToken -> authenticationDataRepository.save(refreshedToken) }
            .subscribe({
                uiState.update { it.copy(refreshAuthenticationResult = RefreshAuthenticationResult.Success) }
            }, {
                uiState.update { it.copy(refreshAuthenticationResult = RefreshAuthenticationResult.Failure) }
            })
    }

    private fun refreshTokenOrThrow(optionalAuthenticationData: Optional<AuthenticationData>): Single<AuthenticationData> {
        if (optionalAuthenticationData.isPresent) {
            return authenticationService.refreshToken(optionalAuthenticationData.get().refreshToken)
        }
        return Single.error(AuthenticationDataNotExistsException)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}

data class EntryUiState(val refreshAuthenticationResult: RefreshAuthenticationResult? = null)

sealed interface RefreshAuthenticationResult {
    object Success : RefreshAuthenticationResult
    object Failure : RefreshAuthenticationResult
}

private object AuthenticationDataNotExistsException : Exception("No token has been persisted yet")