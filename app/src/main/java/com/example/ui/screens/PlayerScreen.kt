package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoLesson
import com.example.ui.AppScreen
import com.example.ui.EduVideoViewModel
import com.example.ui.components.EduVideoPlayer
import com.example.ui.components.QuizCompanion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: EduVideoViewModel,
    lesson: VideoLesson,
    modifier: Modifier = Modifier
) {
    var quizSelectedTab by remember { mutableStateOf(2) } // Default to Script (now index 2) first, until video finishes!

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = lesson.topic,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${lesson.studentClass} • ${lesson.durationMinutes} min timeline",
                            fontSize = 11.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.setScreen(AppScreen.Home) },
                        modifier = Modifier.testTag("player_back_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back to Home", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. The Immersive Custom Video Player
            EduVideoPlayer(
                viewModel = viewModel,
                lesson = lesson,
                onVideoFinished = {
                    // Video finished playing! Automatically switch active tab to Quiz
                    quizSelectedTab = 0
                }
            )

            // Dynamic recommendation tip on completing the video
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Tip: Play the full lesson video, then try the Quiz tab below to test your mastery!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondary,
                        lineHeight = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Interactive Study & Quiz Companion
            // Key is used to reconstruct companion tabs when video changes or finishes
            key(lesson.id, quizSelectedTab) {
                QuizCompanion(
                    viewModel = viewModel,
                    lesson = lesson,
                    selectedQuizTab = quizSelectedTab
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
