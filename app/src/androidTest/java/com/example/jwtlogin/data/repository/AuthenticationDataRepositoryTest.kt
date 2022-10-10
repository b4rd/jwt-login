package com.example.jwtlogin.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.datastore.rxjava3.RxDataStore
import androidx.test.core.app.ApplicationProvider
import com.example.jwtlogin.data.model.AuthenticationData
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*

private const val TEST_DATASTORE_NAME: String = "test_datastore"

@ExperimentalCoroutinesApi
class AuthenticationDataRepositoryTest {

    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testDataStore: RxDataStore<Preferences> =
        RxPreferenceDataStoreBuilder(testContext, TEST_DATASTORE_NAME).build()
    private val repository = AuthenticationDataRepository(testDataStore, Gson())

    @Before
    fun setUp() {
        // reset the data store
        testDataStore.updateDataAsync { t ->
            val mutablePrefs = t.toMutablePreferences()
            mutablePrefs.clear()
            Single.just(mutablePrefs)
        }.blockingGet()
    }

    @Test
    fun noAuthDataInitially() {
        val authData = repository.fetch().first(Optional.empty()).blockingGet()
        assertEquals(Optional.empty<AuthenticationData>(), authData)
    }

    @Test
    fun saveReturnsTheSavedAuthData() {
        val authDataToSave = AuthenticationData("test token")
        val saveResult = repository.save(authDataToSave).blockingGet()
        assertEquals(authDataToSave, saveResult)
    }

    @Test
    fun fetchReturnsTheSavedAuthData() {
        val storedAuthData = AuthenticationData("test token")
        repository.save(storedAuthData).blockingGet()

        val fetchedAuthData = repository.fetch().first(Optional.empty()).blockingGet().get()
        assertEquals(storedAuthData, fetchedAuthData)
    }

    @After
    fun cleanUp() {
        testDataStore.dispose()
    }
}