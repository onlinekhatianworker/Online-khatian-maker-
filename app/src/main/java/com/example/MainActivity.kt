package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AboutAppScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KhatianFormScreen
import com.example.ui.screens.PreviewScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UsageGuideScreen
import com.example.ui.theme.OnlineKhatianTheme
import com.example.viewmodel.KhatianViewModel

const val ROUTE_HOME = "home"
const val ROUTE_FORM = "khatian_form"
const val ROUTE_PREVIEW = "khatian_preview"
const val ROUTE_SETTINGS = "settings"
const val ROUTE_GUIDE = "usage_guide"
const val ROUTE_PRIVACY = "privacy_policy"
const val ROUTE_ABOUT = "about_app"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Strict zero-persistence: never restore savedInstanceState for user inputs
        super.onCreate(null)
        enableEdgeToEdge()
        setContent {
            OnlineKhatianTheme {
                val navController = rememberNavController()
                val khatianViewModel: KhatianViewModel = viewModel()
                val settings by khatianViewModel.settings.collectAsStateWithLifecycle()
                val draft by khatianViewModel.draft.collectAsStateWithLifecycle()
                val previewState by khatianViewModel.previewState.collectAsStateWithLifecycle()

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_HOME,
                ) {
                    composable(ROUTE_HOME) {
                        HomeScreen(
                            settings = settings,
                            onSelectSurvey = { survey ->
                                khatianViewModel.selectSurvey(survey)
                                navController.navigate(ROUTE_FORM)
                            },
                            onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
                            onOpenGuide = { navController.navigate(ROUTE_GUIDE) },
                            onOpenPrivacy = { navController.navigate(ROUTE_PRIVACY) },
                            onOpenAbout = { navController.navigate(ROUTE_ABOUT) },
                        )
                    }

                    composable(ROUTE_FORM) {
                        KhatianFormScreen(
                            draft = draft,
                            settings = settings,
                            viewModel = khatianViewModel,
                            onBackHome = {
                                khatianViewModel.clearPreviewBitmaps()
                                navController.popBackStack()
                            },
                            onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
                            onOpenPreview = { navController.navigate(ROUTE_PREVIEW) },
                        )
                    }

                    composable(ROUTE_PREVIEW) {
                        PreviewScreen(
                            draft = draft,
                            settings = settings,
                            previewState = previewState,
                            viewModel = khatianViewModel,
                            onBackToEdit = {
                                khatianViewModel.clearPreviewBitmaps()
                                navController.popBackStack()
                            },
                        )
                    }

                    composable(ROUTE_SETTINGS) {
                        SettingsScreen(
                            settings = settings,
                            viewModel = khatianViewModel,
                            onBack = { navController.popBackStack() },
                            onOpenGuide = { navController.navigate(ROUTE_GUIDE) },
                            onOpenPrivacy = { navController.navigate(ROUTE_PRIVACY) },
                            onOpenAbout = { navController.navigate(ROUTE_ABOUT) },
                        )
                    }

                    composable(ROUTE_GUIDE) {
                        UsageGuideScreen(
                            settings = settings,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(ROUTE_PRIVACY) {
                        PrivacyPolicyScreen(
                            settings = settings,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(ROUTE_ABOUT) {
                        AboutAppScreen(
                            settings = settings,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}
