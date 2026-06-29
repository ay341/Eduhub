package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuizQuestion
import com.example.data.VideoLesson

@Composable
fun QuizCompanion(
    lesson: VideoLesson,
    selectedQuizTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(selectedQuizTab) }
    val tabs = listOf("Quiz", "Flashcards", "Script", "Notes")

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tab Selector
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) },
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (activeTab == index) MaterialTheme.colorScheme.primary else Color(0xFF49454F)
                            )
                        }
                    )
                }
            }

            // Tab Content Frame
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                },
                label = "TabContent"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> InteractiveQuizTab(quiz = lesson.quiz)
                    1 -> FlashcardsTab(lesson = lesson)
                    2 -> NarrationScriptTab(lesson = lesson)
                    3 -> StudyNotesTab(lesson = lesson)
                }
            }
        }
    }
}

@Composable
fun InteractiveQuizTab(quiz: List<QuizQuestion>) {
    if (quiz.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No quiz questions generated for this lesson.", color = Color(0xFF49454F), fontSize = 14.sp)
        }
        return
    }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var answeredCount by remember { mutableStateOf(0) }
    var showResults by remember { mutableStateOf(false) }

    val question = quiz.getOrNull(currentQuestionIndex)

    if (showResults) {
        // Render Score report card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFD1FAE5), RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = "Stars Celebration",
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(48.dp)
                )
            }

            Text(
                text = "Lesson Quiz Completed!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Your Score: $score / ${quiz.size}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            val feedbackText = when {
                score == quiz.size -> "✦ Perfect Score! Outstanding Genius! ✦"
                score >= quiz.size / 2 -> "✦ Great job! You captured key concepts! ✦"
                else -> "Keep learning! Watch the video lesson once more to boost your score!"
            }

            Text(
                text = feedbackText,
                color = Color(0xFF49454F),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Button(
                onClick = {
                    currentQuestionIndex = 0
                    selectedAnswerIndex = null
                    score = 0
                    answeredCount = 0
                    showResults = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .testTag("quiz_retry_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry Quiz", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    } else if (question != null) {
        // Render active question
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quiz Progress Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUESTION ${currentQuestionIndex + 1} OF ${quiz.size}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Score: $score",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Question Text
            Text(
                text = question.question,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            // Multiple Choice Options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                question.options.forEachIndexed { index, option ->
                    val isSelected = selectedAnswerIndex == index
                    val isCorrect = question.correctAnswerIndex == index
                    val hasAnswered = selectedAnswerIndex != null

                    val optionBorderColor = when {
                        isSelected && isCorrect -> Color(0xFF10B981) // Green border for correct answer
                        isSelected && !isCorrect -> Color(0xFFEF4444) // Red border for selected incorrect
                        hasAnswered && isCorrect -> Color(0xFF10B981) // Highlight correct answer on wrong click
                        else -> MaterialTheme.colorScheme.outlineVariant // Default border
                    }

                    val optionBgColor = when {
                        isSelected && isCorrect -> Color(0xFFECFDF5)
                        isSelected && !isCorrect -> Color(0xFFFEF2F2)
                        hasAnswered && isCorrect -> Color(0xFFECFDF5)
                        else -> Color(0xFFF7F2FA)
                    }

                    val optionIndicatorIcon = when {
                        hasAnswered && isCorrect -> Icons.Default.CheckCircle
                        isSelected && !isCorrect -> Icons.Default.Cancel
                        else -> null
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(optionBgColor)
                            .border(1.dp, optionBorderColor, RoundedCornerShape(12.dp))
                            .clickable(enabled = !hasAnswered) {
                                selectedAnswerIndex = index
                                answeredCount++
                                if (index == question.correctAnswerIndex) {
                                    score++
                                }
                            }
                            .padding(12.dp)
                            .testTag("quiz_option_$index"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Letter option avatar
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    if (isSelected) optionBorderColor else MaterialTheme.colorScheme.outlineVariant,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ('A' + index).toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = option,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        if (optionIndicatorIcon != null) {
                            Icon(
                                imageVector = optionIndicatorIcon,
                                contentDescription = null,
                                tint = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Explanation Section (Only shown after answering)
            if (selectedAnswerIndex != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Did You Know?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = question.explanation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Next Button
                Button(
                    onClick = {
                        if (currentQuestionIndex < quiz.lastIndex) {
                            currentQuestionIndex++
                            selectedAnswerIndex = null
                        } else {
                            showResults = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_next_button")
                ) {
                    Text(
                        text = if (currentQuestionIndex == quiz.lastIndex) "See Final Score" else "Next Question",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (currentQuestionIndex == quiz.lastIndex) Icons.Default.Check else Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NarrationScriptTab(lesson: VideoLesson) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(lesson.scenes) { index, scene ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7F2FA), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scene ${index + 1}: ${scene.title}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${scene.durationSeconds}s",
                            fontSize = 10.sp,
                            color = Color(0xFF49454F),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = scene.voiceoverText,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StudyNotesTab(lesson: VideoLesson) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(lesson.scenes) { index, scene ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7F2FA), RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "CONCEPT ${index + 1}: ${scene.title.uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    // Splitting points and rendering
                    val points = scene.textOnScreen.split("\n")
                    points.forEach { point ->
                        if (point.isNotBlank()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = point.trim().removePrefix("•").removePrefix("-").trim(),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlashcardsTab(lesson: VideoLesson) {
    val scenes = lesson.scenes
    if (scenes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No content available for flashcards.", color = Color(0xFF49454F), fontSize = 14.sp)
        }
        return
    }

    var currentCardIndex by remember { mutableStateOf(0) }
    val currentScene = scenes[currentCardIndex]
    
    // Reset flip state when card changes
    var isFlipped by remember(currentCardIndex) { mutableStateOf(false) }
    
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "CardFlipRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Flashcard 3D Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 8 * density
                }
                .clip(RoundedCornerShape(16.dp))
                .clickable { isFlipped = !isFlipped }
                .background(
                    if (rotation <= 90f) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    }
                )
                .border(
                    width = 1.5.dp,
                    color = if (rotation <= 90f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // Front Side
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "CONCEPT ${currentCardIndex + 1}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentScene.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Flip Icon",
                            tint = Color(0xFF79747E),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tap Card to Flip",
                            fontSize = 11.sp,
                            color = Color(0xFF79747E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Back Side (mirrored back using rotationY = 180f)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = 180f
                        },
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "KEY TAKEAWAYS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    val points = currentScene.textOnScreen.split("\n")
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        points.forEach { point ->
                            if (point.isNotBlank()) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = point.trim().removePrefix("•").removePrefix("-").trim(),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Navigation Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (currentCardIndex > 0) {
                        currentCardIndex--
                    }
                },
                enabled = currentCardIndex > 0
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous card"
                )
            }

            Text(
                text = "Card ${currentCardIndex + 1} of ${scenes.size}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            IconButton(
                onClick = {
                    if (currentCardIndex < scenes.lastIndex) {
                        currentCardIndex++
                    }
                },
                enabled = currentCardIndex < scenes.lastIndex
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next card"
                )
            }
        }
    }
}
