package com.example.jwtlogin.data.repository

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.rxjava3.RxDataStore
import com.example.jwtlogin.data.model.AuthenticationData
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationDataRepository @Inject constructor(
    private val dataStore: RxDataStore<Preferences>,
    private val gson: Gson,
) {

    @ExperimentalCoroutinesApi
    fun save(authenticationData: AuthenticationData): Single<AuthenticationData> {
        return dataStore.updateDataAsync { t ->
            val mutablePreferences: MutablePreferences = t.toMutablePreferences()
            mutablePreferences[AUTH_DATA] = authenticationData.toJson()
            Single.just(mutablePreferences)
        }.map { authenticationData }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun fetch(): Flowable<Optional<AuthenticationData>> {
        return dataStore.data()
            .map { Optional.ofNullable(it[AUTH_DATA]) }
            .map { optAuthDataAsString -> optAuthDataAsString.map { it.toAuthenticationData() } }
    }

    private fun AuthenticationData.toJson(): String {
        return gson.toJson(this)
    }

    private fun String.toAuthenticationData(): AuthenticationData {
        return gson.fromJson(this, AuthenticationData::class.java)
    }

    companion object {
        val AUTH_DATA = stringPreferencesKey("authenticationData")
    }
}