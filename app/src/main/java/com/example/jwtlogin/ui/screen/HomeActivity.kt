package com.example.jwtlogin.ui.screen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jwtlogin.R
import com.example.jwtlogin.ui.theme.JwtLoginTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JwtLoginTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    val homeUiState by homeViewModel.uiState.collectAsState()
    UserInfoDisplay(homeUiState.userInfo)

    LaunchedEffect(Unit) {
        homeViewModel.init()
    }
}

@Composable
private fun UserInfoDisplay(
    userInfo: UserInfo,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        HeaderText(stringResource(R.string.username))
        Text(
            text = userInfo.name,
            modifier = Modifier.testTag("userName")
        )

        Spacer(modifier = Modifier.height(8.dp))

        HeaderText(stringResource(R.string.role))
        Text(
            text = userInfo.role,
            modifier = Modifier.testTag("userRole")
        )
    }
}

@Composable
private fun HeaderText(text: String) {
    Text(
        style = MaterialTheme.typography.h4,
        color = MaterialTheme.colors.primary,
        text = text
    )
}

fun Context.getHomeIntent(): Intent {
    return Intent(this, HomeActivity::class.java)
}