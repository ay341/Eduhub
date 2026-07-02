package com.example.ui.components.learningjourney

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoLesson
import com.example.ui.EduVideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationModalScreen(
    viewModel: EduVideoViewModel,
    lesson: VideoLesson,
    onClose: () -> Unit
) {
    val topic = lesson.topic
    
    val easierTopic = "Introduction to $topic"
    val similarTopic = "$topic Core Applications"
    val advancedTopic = "Advanced Advanced Principles of $topic"

    fun generateRecommendation(targetTopic: String) {
        viewModel.topic.value = targetTopic
        viewModel.generateVideoLesson()
        onClose()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Adaptive Recommendations", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CompassCalibration,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Adaptive Personalized Paths",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Text(
                text = "Choose an adaptive study next-step customized exactly to your current mastery and academic performance details.",
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            RecommendationCard(
                title = "Easier Topic Path",
                description = "Build foundational prerequisites and basic terminologies step-by-step.",
                targetTopic = easierTopic,
                onSelect = { generateRecommendation(easierTopic) }
            )

            RecommendationCard(
                title = "Parallel Similar Path",
                description = "Explore adjacent parallel subjects, contextual practicals, and syllabus chapters.",
                targetTopic = similarTopic,
                onSelect = { generateRecommendation(similarTopic) }
            )

            RecommendationCard(
                title = "Advanced Mastery Path",
                description = "Challenge yourself with deep-dive theorems, advanced calculations, and exam prep.",
                targetTopic = advancedTopic,
                onSelect = { generateRecommendation(advancedTopic) }
            )
        }
    }
}

@Composable
fun RecommendationCard(
    title: String,
    description: String,
    targetTopic: String,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = targetTopic,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth().testTag("rec_btn_${title.split(" ")[0].lowercase()}")
            ) {
                Text("Generate Adaptive Lesson")
            }
        }
    }
}
