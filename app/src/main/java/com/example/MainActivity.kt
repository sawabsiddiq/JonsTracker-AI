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
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()

                // Intercept back gestures globally to avoid exiting the app unexpectedly
                BackHandler(enabled = currentScreen !is Screen.Today && currentScreen !is Screen.Onboarding) {
                    viewModel.navigateBack()
                }

                // Decide whether to show bottom navigation (Suppress it on Onboarding, or Detail views)
                val showBottomBar = currentScreen !is Screen.Onboarding &&
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
