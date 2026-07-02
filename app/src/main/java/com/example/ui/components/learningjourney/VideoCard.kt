package com.example.ui.components.learningjourney

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoLesson
import com.example.ui.EduVideoViewModel
import com.example.ui.components.EduVideoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoModalScreen(
    viewModel: EduVideoViewModel,
    lesson: VideoLesson,
    onClose: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Lecture Playback", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EduVideoPlayer(
                viewModel = viewModel,
                lesson = lesson,
                onVideoFinished = {
                    viewModel.updateLessonProgress(lesson.id, videoCompleted = true)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Make sure to watch the full slide-by-slide explanation with text-to-speech narrative to unlock the next milestone steps of your learning journey!",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.updateLessonProgress(lesson.id, videoCompleted = true)
                    onClose()
                },
                modifier = Modifier.fillMaxWidth().testTag("video_complete_button")
            ) {
                Text("Mark Video as Completed")
            }
        }
    }
}
