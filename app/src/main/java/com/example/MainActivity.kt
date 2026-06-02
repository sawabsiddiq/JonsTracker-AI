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
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                BackHandler(enabled = currentScreen !is Screen.Dashboard && currentScreen !is Screen.Onboarding) {
                    viewModel.navigateBack()
                }

                // Decide whether to show bottom navigation (Suppress it on Onboarding, Sync Review, or Detail views)
                val showBottomBar = currentScreen !is Screen.Onboarding &&
                        currentScreen !is Screen.GmailSyncReview &&
                        currentScreen !is Screen.Detail

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.testTag("app_bottom_nav_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Dashboard,
                                    onClick = { viewModel.navigateTo(Screen.Dashboard) },
                                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
                                    label = { Text("Dashboard") },
                                    modifier = Modifier.testTag("nav_item_dashboard")
                                )
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Pipeline,
                                    onClick = { viewModel.navigateTo(Screen.Pipeline) },
                                    icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Board info") },
                                    label = { Text("Board") },
                                    modifier = Modifier.testTag("nav_item_pipeline")
                                )
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Analytics,
                                    onClick = { viewModel.navigateTo(Screen.Analytics) },
                                    icon = { Icon(imageVector = Icons.Default.Build, contentDescription = "Metrics") },
                                    label = { Text("Metrics") },
                                    modifier = Modifier.testTag("nav_item_analytics")
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(
                                bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp
                            )
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "ScreenSwitchAnimator"
                        ) { screen ->
                            when (screen) {
                                is Screen.Onboarding -> OnboardingScreen(viewModel = viewModel)
                                is Screen.Dashboard -> DashboardScreen(viewModel = viewModel)
                                is Screen.Pipeline -> PipelineScreen(viewModel = viewModel)
                                is Screen.Analytics -> AnalyticsScreen(viewModel = viewModel)
                                is Screen.GmailSyncReview -> GmailSyncScreen(viewModel = viewModel)
                                is Screen.Detail -> DetailScreen(applicationId = screen.applicationId, viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
