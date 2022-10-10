package com.example.jwtlogin.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jwtlogin.R
import com.example.jwtlogin.ui.theme.JwtLoginTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JwtLoginTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LoginScreen()
                }
            }
        }
    }
}

@Composable
private fun LoginScreen(loginViewModel: LoginViewModel = viewModel()) {
    val loginUiState by loginViewModel.uiState.collectAsState()

    LoginContent(
        loginUiState = loginUiState,
        onLoginClicked = loginViewModel::login
    )

    when (val loginResult = loginUiState.loginResult) {
        is LoginResult.Success -> {
            with(LocalContext.current as Activity) {
                startActivity(getHomeIntent())
                finish()
            }
        }
        is LoginResult.Failure -> {
            ErrorDialog(loginResult) { loginViewModel.onErrorDialogDismissed() }
        }
        else -> {}
    }
}

@Composable
private fun LoginContent(
    loginUiState: LoginUiState,
    modifier: Modifier = Modifier,
    onLoginClicked: (userName: String, password: String) -> Unit,
) {
    Column(
        modifier = modifier
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        var username by rememberSaveable { mutableStateOf("") }
        TextField(
            label = { Text(text = stringResource(R.string.username)) },
            enabled = !loginUiState.loading,
            value = username,
            onValueChange = { username = it },
        )
        Spacer(modifier = Modifier.height(16.dp))

        var password by remember { mutableStateOf("") }
        PasswordField(
            password = password,
            onValueChange = { password = it },
            enabled = !loginUiState.loading,
        )
        Spacer(modifier = Modifier.height(24.dp))

        LoginButton(
            loading = loginUiState.loading,
            enabled = username.isNotEmpty() && password.isNotEmpty(),
            onLoginClicked = { onLoginClicked(username, password) }
        )
    }
}

@Composable
private fun PasswordField(
    password: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
        label = { Text(text = stringResource(R.string.password)) },
        value = password,
        modifier = modifier,
        enabled = enabled,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

            val description = if (passwordVisible)
                stringResource(R.string.hide_password)
            else stringResource(R.string.show_password)

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, description)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        onValueChange = onValueChange
    )
}

@Composable
private fun LoginButton(
    loading: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onLoginClicked: () -> Unit,
) {
    Button(
        onClick = { if (!loading) onLoginClicked() },
        enabled = enabled,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.login),
            style = MaterialTheme.typography.button,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (loading) {
            Spacer(modifier = Modifier.width(24.dp))
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ErrorDialog(
    failure: LoginResult.Failure,
    onDismissed: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissed,
        text = { Text(failure.getErrorMessage()) },
        confirmButton = {
            Button(onClick = onDismissed) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        }
    )
}

@Composable
@ReadOnlyComposable
private fun LoginResult.Failure.getErrorMessage(): String {
    val resId = when (this) {
        LoginResult.Failure.InvalidCredentials -> R.string.invalid_credentials
        LoginResult.Failure.NoInternet -> R.string.no_internet
        LoginResult.Failure.UnknownError -> R.string.unknown_error
    }
    return stringResource(id = resId)
}

fun Context.getLoginIntent(): Intent {
    return Intent(this, LoginActivity::class.java)
}