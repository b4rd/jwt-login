package com.example.jwtlogin.ui.screen

import androidx.lifecycle.ViewModel
import com.example.jwtlogin.data.model.AuthenticationData
import com.example.jwtlogin.data.repository.AuthenticationDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authenticationDataRepository: AuthenticationDataRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(HomeUiState())
    private val compositeDisposable = CompositeDisposable()

    fun init() {
        fetchExistingAuthData()
            .map { authData -> authData.getClaims() }
            .subscribe { claims ->
                uiState.update { it.copy(userInfo = UserInfo(claims.fullName, claims.role)) }
            }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    private fun fetchExistingAuthData(): Maybe<AuthenticationData> {
        return authenticationDataRepository.fetch()
            .first(Optional.empty())
            .flatMapMaybe { optAuthData ->
                if (optAuthData.isPresent) Maybe.just(optAuthData.get()) else Maybe.never()
            }
    }
}

data class UserInfo(val name: String, val role: String)

data class HomeUiState(val userInfo: UserInfo = UserInfo("", ""))