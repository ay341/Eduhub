package com.example.ui.components.learningjourney

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoLesson
import com.example.ui.EduVideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryModalScreen(
    viewModel: EduVideoViewModel,
    lesson: VideoLesson,
    onClose: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Conceptual Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Short summary card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Fast High-Level Summary",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = lesson.shortExplanation,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Detailed explanation card
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Detailed Academic Breakdown",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = lesson.detailedExplanation,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Real-life examples
            if (lesson.realLifeExamples.isNotEmpty()) {
                Text("Real-World Scenarios & Phenomena", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                lesson.realLifeExamples.forEach { example ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Park, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = example, fontSize = 12.sp, lineHeight = 16.sp)
                        }
                    }
                }
            }

            // Common Mistakes
            if (lesson.commonMistakes.isNotEmpty()) {
                Text("Common Academic Misconceptions & Mistakes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                lesson.commonMistakes.forEach { mistake ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = mistake, fontSize = 12.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            // Practical Applications
            if (lesson.practicalApplications.isNotEmpty()) {
                Text("Direct Practical & Field Applications", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                lesson.practicalApplications.forEach { app ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = app, fontSize = 12.sp, lineHeight = 16.sp)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.updateLessonProgress(lesson.id, summaryRead = true)
                    onClose()
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).testTag("summary_complete_button")
            ) {
                Text("Mark Summary as Read")
            }
        }
    }
}
