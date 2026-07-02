package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.VideoLessonRepository
import com.example.ui.AppScreen
import com.example.ui.EduVideoViewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GeneratingScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.LearningJourneyScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Initialize Room DB and repository directly
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = VideoLessonRepository(database.videoLessonDao())
            
            // Retrieve the ViewModel inside Compose to prevent complex Activity logic
            val viewModel: EduVideoViewModel = viewModel(
                factory = EduVideoViewModel.Factory(repository, applicationContext)
            )

            // Observe theme state
            val isDarkTheme by viewModel.isDarkMode.collectAsState()

            androidx.compose.runtime.CompositionLocalProvider(
                com.example.ui.theme.LocalThemeContext provides com.example.ui.theme.ThemeContextState(
                    isDark = isDarkTheme,
                    toggleTheme = { viewModel.toggleDarkMode() }
                )
            ) {
                MyApplicationTheme(darkTheme = isDarkTheme) {
                    // Observe states
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val recentLessons by viewModel.lessonHistory.collectAsState()
                    val activeLesson by viewModel.activeLesson.collectAsState()
                    val generateState by viewModel.generateState.collectAsState()

                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AppScreen.Auth -> {
                                AuthScreen(viewModel = viewModel)
                            }
                            AppScreen.Dashboard -> {
                                DashboardScreen(viewModel = viewModel)
                            }
                            AppScreen.Home -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    recentLessons = recentLessons
                                )
                            }
                            AppScreen.Generating -> {
                                GeneratingScreen(
                                    viewModel = viewModel,
                                    generateState = generateState
                                )
                            }
                            AppScreen.Player -> {
                                if (activeLesson != null) {
                                    PlayerScreen(
                                        viewModel = viewModel,
                                        lesson = activeLesson!!
                                    )
                                } else {
                                    viewModel.setScreen(AppScreen.Home)
                                }
                            }
                            AppScreen.Library -> {
                                LibraryScreen(
                                    viewModel = viewModel,
                                    lessons = recentLessons
                                )
                            }
                            AppScreen.Profile -> {
                                ProfileScreen(viewModel = viewModel)
                            }
                            AppScreen.LearningJourney -> {
                                if (activeLesson != null) {
                                    LearningJourneyScreen(
                                        viewModel = viewModel,
                                        lesson = activeLesson!!
                                    )
                                } else {
                                    viewModel.setScreen(AppScreen.Home)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
