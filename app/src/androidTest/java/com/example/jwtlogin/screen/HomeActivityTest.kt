package com.example.jwtlogin.screen

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.rxjava3.RxDataStore
import com.example.jwtlogin.data.model.AuthenticationData
import com.example.jwtlogin.data.repository.AuthenticationDataRepository
import com.example.jwtlogin.ui.screen.HomeActivity
import com.example.jwtlogin.utils.waitUntilExists
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class HomeActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject lateinit var dataStore: RxDataStore<Preferences>
    @Inject lateinit var authenticationDataRepository: AuthenticationDataRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun noUserInfoIsDisplayedInitially() {
        composeTestRule.onNodeWithTag("userName").assertTextEquals("")
        composeTestRule.onNodeWithTag("userRole").assertTextEquals("")
    }

    @Test
    fun userInfoIsDisplayedBasedOnThePersistedAuthData() {
        authenticationDataRepository.save(AuthenticationData(TEST_ACCESS_TOKEN)).blockingGet()
        composeTestRule.waitUntilExists(hasTestTag("userName").and(hasText("").not()))

        composeTestRule.onNodeWithTag("userName").assertTextEquals("John Doe")
        composeTestRule.onNodeWithTag("userRole").assertTextEquals("editor")
    }

    @After
    fun tearDown() {
        // avoid having multiple active DataStores
        // https://stackoverflow.com/questions/70847060/there-are-multiple-datastores-active-for-the-same-file-in-hiltandroidtest
        dataStore.dispose()
    }
}

const val TEST_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZHA6dXNlcl9pZCI6IjUwYTdkYTFkLWZlMDctNGMxNC04YjFiLTAwNzczN2Y0Nzc2MyIsImlkcDp1c2VyX25hbWUiOiJqZG9lIiwiaWRwOmZ1bGxuYW1lIjoiSm9obiBEb2UiLCJyb2xlIjoiZWRpdG9yIiwiZXhwIjoxNTU2NDc2MjU1fQ.iqFmotBtfAYLplfpLVh_kPgvOIPyV7UMm-NZA06XA5I"
