package com.example.jwtlogin.ui.screen

import androidx.lifecycle.ViewModel
import com.example.jwtlogin.data.repository.AuthenticationDataRepository
import com.example.jwtlogin.data.service.AuthenticationService
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import retrofit2.HttpException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authenticationService: AuthenticationService,
    private val authenticationDataRepository: AuthenticationDataRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(LoginUiState())
    private val compositeDisposable = CompositeDisposable()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun login(username: String, password: String) {
        uiState.update { it.copy(loading = true) }

        compositeDisposable.add(
            authenticationService.login(username, password)
                .flatMap { token -> authenticationDataRepository.save(token) }
                .subscribe({
                    uiState.update { it.copy(loading = false, loginResult = LoginResult.Success) }
                }, { throwable ->
                    uiState.update { it.copy(loading = false, loginResult = throwable.toLoginFailure()) }
                })
        )
    }

    fun onErrorDialogDismissed() {
        uiState.update { it.copy(loginResult = null) }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}

data class LoginUiState(val loading: Boolean = false, val loginResult: LoginResult? = null)

sealed interface LoginResult {
    object Success : LoginResult

    sealed interface Failure : LoginResult {
        object InvalidCredentials : Failure
        object NoInternet : Failure
        object UnknownError : Failure
    }
}

private fun Throwable.toLoginFailure(): LoginResult.Failure {
    return when (this) {
        is UnknownHostException -> LoginResult.Failure.NoInternet
        is HttpException -> {
            return if (code() == 401) {
                LoginResult.Failure.InvalidCredentials
            } else {
                LoginResult.Failure.UnknownError
            }
        }
        else -> LoginResult.Failure.UnknownError
    }
}