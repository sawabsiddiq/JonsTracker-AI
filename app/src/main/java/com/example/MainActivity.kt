package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLog

class MainActivity : ComponentActivity() {
    
    // Instantiate our shared ViewModel
    private val viewModel: JobTrackerViewModel by viewModels {
        JobTrackerViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup edge-to-edge full screen
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val useDarkTheme = when (themeMode) {
                com.example.data.local.AppThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                com.example.data.local.AppThemeMode.LIGHT -> false
                com.example.data.local.AppThemeMode.DARK -> true
            }

            val triggerSignIn by viewModel.triggerGoogleSignIn.collectAsState()
            val recoverableAuthIntent by viewModel.recoverableAuthIntent.collectAsState()

            val gso = remember {
                com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/gmail.readonly"))
                    .build()
            }
            val googleSignInClient = remember { com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso) }

            val googleSignInLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
            ) { result ->
                viewModel.onGoogleSignInComplete()
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                        if (account != null) {
                            viewModel.retrieveAndSaveTokenForAccount(account)
                        } else {
                            viewModel.setSyncError("Google Account not found.")
                        }
                    } catch (e: Exception) {
                        AppLog.e("MainActivity", "Google Sign-In failed.", e)
                        viewModel.setSyncError("Google Sign-In failed. Please try reconnecting your account.")
                    }
                } else {
                    viewModel.setSyncError("Google Sign-In cancelled or failed.")
                }
            }

            val recoverableAuthLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
            ) { result ->
                viewModel.clearRecoverableAuthIntent()
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    val lastAccount = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(this)
                    if (lastAccount != null) {
                        viewModel.retrieveAndSaveTokenForAccount(lastAccount)
                    }
                } else {
                    viewModel.setSyncError("Gmail readonly access authorization denied.")
                }
            }

            LaunchedEffect(triggerSignIn) {
                if (triggerSignIn) {
                    googleSignInClient.signOut().addOnCompleteListener {
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    }
                }
            }

            LaunchedEffect(recoverableAuthIntent) {
                recoverableAuthIntent?.let { intent ->
                    recoverableAuthLauncher.launch(intent)
                }
            }

            MyApplicationTheme(darkTheme = useDarkTheme) {
                val currentScreen by viewModel.currentScreen.collectAsState()

                // Intercept back gestures globally to avoid exiting the app unexpectedly
                BackHandler(enabled = currentScreen !is Screen.Today && currentScreen !is Screen.Onboarding) {
                    viewModel.navigateBack()
                }

                // Decide whether to show bottom navigation (Suppress it on Onboarding, Settings, or Detail views)
                val showBottomBar = currentScreen !is Screen.Onboarding &&
                        currentScreen !is Screen.Settings &&
                        currentScreen !is Screen.Detail

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(top = innerPadding.calculateTopPadding())
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "ScreenSwitchAnimator",
                            modifier = Modifier.fillMaxSize()
                        ) { screen ->
                            when (screen) {
                                is Screen.Onboarding -> OnboardingScreen(viewModel = viewModel)
                                is Screen.Today -> TodayScreen(viewModel = viewModel)
                                is Screen.Applications -> ApplicationsScreen(viewModel = viewModel)
                                is Screen.AiInbox -> AiInboxScreen(viewModel = viewModel)
                                is Screen.Pipeline -> PipelineScreen(viewModel = viewModel)
                                is Screen.Insights -> InsightsScreen(viewModel = viewModel)
                                is Screen.Settings -> SettingsScreen(viewModel = viewModel)
                                is Screen.Detail -> DetailScreen(applicationId = screen.applicationId, viewModel = viewModel)
                            }
                        }

                        if (showBottomBar) {
                            FloatingBottomNav(
                                currentScreen = currentScreen,
                                onNavigate = { viewModel.navigateTo(it) },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .testTag("app_bottom_nav_bar")
                            )
                        }
                    }
                }
            }
        }
    }
}
