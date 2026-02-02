package com.example.takeanote1.ui.auth


import android.app.Activity
import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
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
    object LoggedOut : AuthUiState()         // for logout
    object SwitchAccountRequired : AuthUiState() // for switch account
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
            Log.d("LoginScreen", "Launcher: Sign-in activity successful, handling result")
            googleSignInManager.handleSignInResult(
                data = result.data,
                onSuccess = { account ->
                    Log.d("LoginScreen", "Launcher: Google handleSignInResult success for ${account.email}")
                    viewModel.signInWithGoogle(account)
                },
                onError = { message ->
                    Log.e("LoginScreen", "Launcher: Google handleSignInResult error: $message")
                }
            )
        } else {
            Log.w("LoginScreen", "Launcher: Sign-in activity cancelled or failed")
        }
    }

    LaunchedEffect(uiState) {
        Log.d("LoginScreen", "LaunchedEffect: uiState changed to $uiState")
        if (uiState is AuthUiState.Success) {
            Log.d("LoginScreen", "LaunchedEffect: Success state detected, navigating to home")
            onLoginSuccess()
        }
    }

    LoginContent(
        uiState = uiState,
        onGoogleLoginClick = {
            Log.d("LoginScreen", "onGoogleLoginClick: Launching Google Sign-In intent")
            launcher.launch(googleSignInManager.signIn())
        }
    )
}

// ------------------- LOGIN CONTENT -------------------
@Composable
fun LoginContent(
    uiState: AuthUiState,
    onGoogleLoginClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.app_name),
                showBack = false
            )
        }
    ) { paddingValues ->

        if (isPortrait) {
            //KEEP WEIGHTED LAYOUT
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    RotatingQuotesWithIcons(
                        quotesWithIcons = listOf(
                            stringResource(R.string.welcome) to R.drawable.ic_android,
                            stringResource(R.string.keep_notes_safe) to R.drawable.ic_security,
                            stringResource(R.string.get_reminders) to R.drawable.ic_alert,
                            stringResource(R.string.organize_thoughts) to R.drawable.ic_note,
                            stringResource(R.string.start_journey) to R.drawable.ic_rocket
                        ),
                        rotationTime = 2200L
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Button(
                        onClick = onGoogleLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = uiState !is AuthUiState.Loading
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(R.drawable.ic_google_logo),
                                    contentDescription = "Google",
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.sign_in_google),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                    }

                    if (uiState is AuthUiState.Error) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            // ✅ LANDSCAPE — SCROLL SAFE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(16.dp))

                RotatingQuotesWithIcons(
                    quotesWithIcons = listOf(
                        stringResource(R.string.welcome) to R.drawable.ic_android,
                        stringResource(R.string.keep_notes_safe) to R.drawable.ic_security,
                        stringResource(R.string.get_reminders) to R.drawable.ic_alert,
                        stringResource(R.string.organize_thoughts) to R.drawable.ic_note,
                        stringResource(R.string.start_journey) to R.drawable.ic_rocket
                    ),
                    rotationTime = 2200L
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onGoogleLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = uiState !is AuthUiState.Loading
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.ic_google_logo),
                                contentDescription = "Google",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.sign_in_google),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


// ------------------- PREVIEW -------------------
@Composable
@PreviewScreenSizes
fun LoginScreenPreview() {
    TakeANoteTheme {
        LoginContent(uiState = AuthUiState.Idle, onGoogleLoginClick = {})
    }
}