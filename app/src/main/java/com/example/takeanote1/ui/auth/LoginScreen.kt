package com.example.takeanote1.ui.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.takeanote1.R
import com.example.takeanote1.data.GoogleSignInManager
import com.example.takeanote1.ui.components.AppTopBar
import com.example.takeanote1.ui.components.RotatingQuotesWithIcons
import com.example.takeanote1.ui.theme.TakeANoteTheme

// ------------------- UI STATE -------------------
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    object LoggedOut : AuthUiState()
    object SwitchAccountRequired : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

// ------------------- LOGIN SCREEN WRAPPER -------------------
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val googleSignInManager = remember { GoogleSignInManager(context as Activity) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("LoginScreen", "Launcher: Activity result received. ResultCode: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            googleSignInManager.handleSignInResult(
                data = result.data,
                onSuccess = { account ->
                    viewModel.signInWithGoogle(account)
                },
                onError = { message ->
                    Log.e("LoginScreen", "Sign-in error: $message")
                }
            )
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess()
        }
    }

    LoginContent(
        uiState = uiState,
        onGoogleLoginClick = { launcher.launch(googleSignInManager.signIn()) }
    )
}

// ------------------- LOGIN CONTENT -------------------
@Composable
fun LoginContent(
    uiState: AuthUiState,
    onGoogleLoginClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.app_name),
                showBack = false,
                showMenu = false
            )
        }
    ) { paddingValues ->

        // Scrollable content to avoid crashes on rotation
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState) // <-- scrollable
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ------------------- ROTATING QUOTES -------------------
            RotatingQuotesWithIcons(
                quotesWithIcons = listOf(
                    stringResource(R.string.welcome) to R.drawable.ic_android,
                    stringResource(R.string.keep_notes_safe) to R.drawable.ic_security,
                    stringResource(R.string.get_reminders) to R.drawable.ic_alert,
                    stringResource(R.string.organize_thoughts) to R.drawable.ic_note,
                    stringResource(R.string.start_journey) to R.drawable.ic_rocket
                ),
                rotationTime = 2000L
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ------------------- GOOGLE SIGN-IN BUTTON -------------------
            Button(
                onClick = onGoogleLoginClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AuthUiState.Loading
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_google_logo),
                            contentDescription = "Google",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.sign_in_google))
                    }
                }
            }

            // ------------------- ERROR MESSAGE -------------------
            if (uiState is AuthUiState.Error) {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ------------------- PREVIEW -------------------
@Composable
@Preview(showBackground = true, showSystemUi = true)
fun LoginScreenPreview() {
    TakeANoteTheme {
        LoginContent(uiState = AuthUiState.Idle, onGoogleLoginClick = {})
    }
}
