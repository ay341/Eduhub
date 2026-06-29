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
import com.example.ui.screens.GeneratingScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize Room DB and repository directly
                val database = AppDatabase.getDatabase(applicationContext)
                val repository = VideoLessonRepository(database.videoLessonDao())
                
                // Retrieve the ViewModel inside Compose to prevent complex Activity logic
                val viewModel: EduVideoViewModel = viewModel(
                    factory = EduVideoViewModel.Factory(repository)
                )

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
                        }
                    }
                }
            }
        }
    }
}
