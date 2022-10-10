package com.example.jwtlogin.ui.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jwtlogin.ui.theme.JwtLoginTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EntryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JwtLoginTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val entryViewModel: EntryViewModel = viewModel()
                    val entryUiState by entryViewModel.uiState.collectAsState()

                    EntryContent(
                        entryUiState = entryUiState,
                        onAuthRefreshSucceeded = {
                            startActivity(getHomeIntent())
                            finish()
                        },
                        onAuthRefreshFailed = {
                            startActivity(getLoginIntent())
                            finish()
                        },
                    )

                    LaunchedEffect(Unit) {
                        entryViewModel.tryRefreshToken()
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryContent(
    entryUiState: EntryUiState,
    modifier: Modifier = Modifier,
    onAuthRefreshSucceeded: () -> Unit,
    onAuthRefreshFailed: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colors.primary,
            strokeWidth = 4.dp,
            modifier = Modifier.size(50.dp)
        )
    }

    when (entryUiState.refreshAuthenticationResult) {
        RefreshAuthenticationResult.Success -> onAuthRefreshSucceeded()
        RefreshAuthenticationResult.Failure -> onAuthRefreshFailed()
        null -> {}
    }
}