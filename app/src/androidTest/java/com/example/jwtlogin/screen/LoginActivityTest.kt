package com.example.jwtlogin.screen

import androidx.annotation.StringRes
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.jwtlogin.R
import com.example.jwtlogin.data.service.AuthenticationService
import com.example.jwtlogin.ui.screen.LoginActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class LoginActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<LoginActivity>()


    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject lateinit var mockAuthenticationService: AuthenticationService

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun loginButtonIsDisabledInitially() {
        composeTestRule.onNodeWithText(LOGIN_BUTTON_TEXT).assertIsNotEnabled()
    }

    @Test
    fun loginButtonIsEnabledWhenInputFieldsAreNotEmpty() {
        composeTestRule.onNodeWithText(stringResource(R.string.username)).performTextInput("bond")
        composeTestRule.onNodeWithText(stringResource(R.string.password)).performTextInput("007")

        composeTestRule.onNodeWithText(LOGIN_BUTTON_TEXT).assertIsEnabled()
    }

    @Test
    fun errorDialogIsDisplayedWhenLoginFails() {
        every {
            mockAuthenticationService.login(any(), any())
        } returns Single.error(Exception())

        composeTestRule.onNodeWithText(stringResource(R.string.username)).performTextInput("bond")
        composeTestRule.onNodeWithText(stringResource(R.string.password)).performTextInput("007")
        composeTestRule.onNodeWithText(LOGIN_BUTTON_TEXT).performClick()

        composeTestRule.onNodeWithText(stringResource(R.string.unknown_error)).assertIsDisplayed()
    }

    private fun stringResource(@StringRes resId: Int): String {
        return composeTestRule.activity.getString(resId)
    }
}

const val LOGIN_BUTTON_TEXT = "Login"
